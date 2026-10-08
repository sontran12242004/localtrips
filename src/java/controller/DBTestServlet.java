package controller;

import dao.DBContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/db-test")
public class DBTestServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        try (
            PrintWriter out = response.getWriter();
            Connection connection =
                    DBContext.getConnection()
        ) {
            DatabaseMetaData metadata =
                    connection.getMetaData();

            out.println("<h1>Ket noi database thanh cong</h1>");
            out.println(
                    "<p>Database: "
                    + connection.getCatalog()
                    + "</p>"
            );
            out.println(
                    "<p>Driver: "
                    + metadata.getDriverName()
                    + "</p>"
            );
        } catch (SQLException e) {
            response.setStatus(500);

            try (PrintWriter out =
                    response.getWriter()) {
                out.println(
                        "<h1>Ket noi database that bai</h1>"
                );
                out.println(
                        "<pre>"
                        + e.getMessage()
                        + "</pre>"
                );
            }
        }
    }
}