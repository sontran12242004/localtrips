package controller;

import dao.PlaceDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Place;

@WebServlet("/place-test")
public class PlaceTestServlet extends HttpServlet {

    private final PlaceDAO placeDAO =
            new PlaceDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        try {
            List<Place> places =
                    placeDAO.findAll();

            try (
                PrintWriter out =
                        response.getWriter()
            ) {
                out.println(
                        "<h1>Danh sach dia diem</h1>"
                );

                out.println(
                        "<p>So luong: "
                        + places.size()
                        + "</p>"
                );

                out.println(
                        "<table border='1' "
                        + "cellpadding='8'>"
                );

                out.println(
                        "<tr>"
                        + "<th>ID</th>"
                        + "<th>Ten dia diem</th>"
                        + "<th>Danh muc</th>"
                        + "<th>Chi phi</th>"
                        + "<th>Rating</th>"
                        + "</tr>"
                );

                for (Place place : places) {
                    out.println("<tr>");

                    out.println(
                            "<td>"
                            + place.getPlaceId()
                            + "</td>"
                    );

                    out.println(
                            "<td>"
                            + escape(
                                    place.getPlaceName()
                            )
                            + "</td>"
                    );

                    out.println(
                            "<td>"
                            + escape(
                                    place.getCategoryName()
                            )
                            + "</td>"
                    );

                    out.println(
                            "<td>"
                            + place.getEstimatedCost()
                            + "</td>"
                    );

                    out.println(
                            "<td>"
                            + place.getRating()
                            + "</td>"
                    );

                    out.println("</tr>");
                }

                out.println("</table>");
            }
        } catch (RuntimeException e) {
            response.setStatus(500);

            try (
                PrintWriter out =
                        response.getWriter()
            ) {
                out.println(
                        "<h1>Doc dia diem that bai</h1>"
                );

                out.println(
                        "<pre>"
                        + escape(e.getMessage())
                        + "</pre>"
                );
            }
        }
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}