package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class GroupFundMemberSummary {
    private int userId;
    private String memberName;
    private String role;
    private BigDecimal contributed = BigDecimal.ZERO;
    private int contributionCount;
    private Timestamp lastContributionAt;

    public int getUserId(){ return userId; }
    public void setUserId(int userId){ this.userId=userId; }
    public String getMemberName(){ return memberName; }
    public void setMemberName(String memberName){ this.memberName=memberName; }
    public String getRole(){ return role; }
    public void setRole(String role){ this.role=role; }
    public BigDecimal getContributed(){ return contributed; }
    public void setContributed(BigDecimal contributed){ this.contributed=contributed==null?BigDecimal.ZERO:contributed; }
    public int getContributionCount(){ return contributionCount; }
    public void setContributionCount(int contributionCount){ this.contributionCount=contributionCount; }
    public Timestamp getLastContributionAt(){ return lastContributionAt; }
    public void setLastContributionAt(Timestamp lastContributionAt){ this.lastContributionAt=lastContributionAt; }
}
