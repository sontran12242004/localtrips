package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.SettlementBalance;
import model.SettlementTransfer;

public class SettlementDAO {

    public List<SettlementBalance> findBalances(
            int tripId
    ) {
        List<SettlementBalance> balances
                = new ArrayList<>();

        String sql
                = "SELECT tm.user_id, u.full_name, "
                + "COALESCE(("
                + "SELECT SUM(e.amount) "
                + "FROM Expenses e "
                + "WHERE e.trip_id = tm.trip_id "
                + "AND e.payer_id = tm.user_id "
                + "AND e.from_group_fund = 0"
                + "), 0) AS paid, "
                + "COALESCE(("
                + "SELECT SUM(ep.share_amount) "
                + "FROM ExpenseParticipants ep "
                + "INNER JOIN Expenses e "
                + "ON e.expense_id = ep.expense_id "
                + "WHERE e.trip_id = tm.trip_id "
                + "AND ep.user_id = tm.user_id "
                + "AND e.from_group_fund = 0"
                + "), 0) AS owed "
                + "FROM TripMembers tm "
                + "INNER JOIN Users u "
                + "ON u.user_id = tm.user_id "
                + "WHERE tm.trip_id = ? "
                + "ORDER BY "
                + "CASE WHEN tm.member_role = 'OWNER' "
                + "THEN 0 ELSE 1 END, "
                + "u.full_name";

        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement
                     = connection.prepareStatement(sql)) {

            statement.setInt(1, tripId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    SettlementBalance balance
                            = new SettlementBalance();

                    balance.setMemberId(
                            resultSet.getInt("user_id")
                    );

                    balance.setMemberName(
                            resultSet.getString("full_name")
                    );

                    balance.setPaid(
                            resultSet.getBigDecimal("paid")
                    );

                    balance.setOwed(
                            resultSet.getBigDecimal("owed")
                    );

                    balance.calculateBalance();
                    balances.add(balance);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Không thể tính số dư thành viên.",
                    e
            );
        }

        return balances;
    }

    public List<SettlementTransfer> calculateTransfers(
            List<SettlementBalance> balances
    ) {
        List<SettlementTransfer> transfers
                = new ArrayList<>();

        List<SettlementParty> debtors
                = new ArrayList<>();

        List<SettlementParty> creditors
                = new ArrayList<>();

        for (SettlementBalance balance : balances) {
            int comparison = balance.getBalance()
                    .compareTo(BigDecimal.ZERO);

            if (comparison < 0) {
                debtors.add(
                        new SettlementParty(
                                balance.getMemberId(),
                                balance.getMemberName(),
                                balance.getBalance().negate()
                        )
                );

            } else if (comparison > 0) {
                creditors.add(
                        new SettlementParty(
                                balance.getMemberId(),
                                balance.getMemberName(),
                                balance.getBalance()
                        )
                );
            }
        }

        int debtorIndex = 0;
        int creditorIndex = 0;

        while (debtorIndex < debtors.size()
                && creditorIndex < creditors.size()) {

            SettlementParty debtor
                    = debtors.get(debtorIndex);

            SettlementParty creditor
                    = creditors.get(creditorIndex);

            BigDecimal transferAmount
                    = debtor.amount.min(creditor.amount);

            if (transferAmount.compareTo(
                    BigDecimal.ZERO
            ) > 0) {
                SettlementTransfer transfer
                        = new SettlementTransfer();

                transfer.setFromMemberId(
                        debtor.memberId
                );

                transfer.setFromMemberName(
                        debtor.memberName
                );

                transfer.setToMemberId(
                        creditor.memberId
                );

                transfer.setToMemberName(
                        creditor.memberName
                );

                transfer.setAmount(transferAmount);

                transfers.add(transfer);
            }

            debtor.amount = debtor.amount.subtract(
                    transferAmount
            );

            creditor.amount = creditor.amount.subtract(
                    transferAmount
            );

            if (debtor.amount.compareTo(
                    BigDecimal.ZERO
            ) == 0) {
                debtorIndex++;
            }

            if (creditor.amount.compareTo(
                    BigDecimal.ZERO
            ) == 0) {
                creditorIndex++;
            }
        }

        return transfers;
    }

    private static class SettlementParty {

        private final int memberId;
        private final String memberName;
        private BigDecimal amount;

        private SettlementParty(
                int memberId,
                String memberName,
                BigDecimal amount
        ) {
            this.memberId = memberId;
            this.memberName = memberName;
            this.amount = amount;
        }
    }
}