package controller.admin;

import dao.AdminAuditLogDAO;
import dao.CategoryDAO;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Category;

@WebServlet({
    "/admin/categories",
    "/admin/categories/create",
    "/admin/categories/edit",
    "/admin/categories/delete"
})
public class AdminCategoryServlet extends HttpServlet {

    private static final String LIST_VIEW = "/WEB-INF/views/admin/category-list.jsp";
    private static final String FORM_VIEW = "/WEB-INF/views/admin/category-form.jsp";

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AdminAuditLogDAO auditLogDAO = new AdminAuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if ("/admin/categories/create".equals(path)) {
            showForm(request, response, null);
            return;
        }

        if ("/admin/categories/edit".equals(path)) {
            showEditForm(request, response);
            return;
        }

        showList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String path = request.getServletPath();

        if ("/admin/categories/create".equals(path)) {
            createCategory(request, response);
            return;
        }

        if ("/admin/categories/edit".equals(path)) {
            updateCategory(request, response);
            return;
        }

        if ("/admin/categories/delete".equals(path)) {
            deleteCategory(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        keyword = keyword == null ? "" : keyword.trim();

        try {
            List<Category> categories = categoryDAO.searchAdmin(keyword);
            request.setAttribute("categories", categories);
            request.setAttribute("keyword", keyword);
            request.setAttribute("pageTitle", "Quản lý danh mục");
            request.getRequestDispatcher(LIST_VIEW).forward(request, response);
        } catch (RuntimeException e) {
            request.setAttribute("categories", java.util.Collections.emptyList());
            request.setAttribute("keyword", keyword);
            request.setAttribute("errorMessage", "Không thể tải danh sách danh mục.");
            request.setAttribute("pageTitle", "Quản lý danh mục");
            request.getRequestDispatcher(LIST_VIEW).forward(request, response);
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int categoryId = parseInt(request.getParameter("id"), -1);
        if (categoryId <= 0) {
            redirectWithMessage(request, response, "Mã danh mục không hợp lệ.", true);
            return;
        }

        Category category = categoryDAO.findById(categoryId);
        if (category == null) {
            redirectWithMessage(request, response, "Không tìm thấy danh mục.", true);
            return;
        }

        showForm(request, response, category);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Category category)
            throws ServletException, IOException {
        request.setAttribute("item", category);
        request.setAttribute("pageTitle", category == null ? "Thêm danh mục" : "Sửa danh mục");
        request.getRequestDispatcher(FORM_VIEW).forward(request, response);
    }

    private void createCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Category category = readForm(request);
        String error = validate(category);

        if (error != null) {
            request.setAttribute("errorMessage", error);
            request.setAttribute("item", category);
            request.setAttribute("pageTitle", "Thêm danh mục");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }

        try {
            int id = categoryDAO.insert(category);
            logAction(request, "CREATE_CATEGORY", "Tạo danh mục #" + id + " - " + category.getCategoryName());
            redirectWithMessage(request, response, "Đã thêm danh mục thành công.", false);
        } catch (RuntimeException e) {
            request.setAttribute("errorMessage", "Không thể thêm danh mục. Mã hoặc tên danh mục có thể đã tồn tại.");
            request.setAttribute("item", category);
            request.setAttribute("pageTitle", "Thêm danh mục");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
        }
    }

    private void updateCategory(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int categoryId = parseInt(request.getParameter("categoryId"), -1);
        if (categoryId <= 0) {
            redirectWithMessage(request, response, "Mã danh mục không hợp lệ.", true);
            return;
        }

        Category category = readForm(request);
        category.setCategoryId(categoryId);
        String error = validate(category);

        if (error != null) {
            request.setAttribute("errorMessage", error);
            request.setAttribute("item", category);
            request.setAttribute("pageTitle", "Sửa danh mục");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
            return;
        }

        try {
            if (!categoryDAO.update(category)) {
                redirectWithMessage(request, response, "Không tìm thấy danh mục để cập nhật.", true);
                return;
            }
            logAction(request, "UPDATE_CATEGORY", "Cập nhật danh mục #" + categoryId + " - " + category.getCategoryName());
            redirectWithMessage(request, response, "Đã cập nhật danh mục thành công.", false);
        } catch (RuntimeException e) {
            request.setAttribute("errorMessage", "Không thể cập nhật danh mục. Mã hoặc tên danh mục có thể đã tồn tại.");
            request.setAttribute("item", category);
            request.setAttribute("pageTitle", "Sửa danh mục");
            request.getRequestDispatcher(FORM_VIEW).forward(request, response);
        }
    }

    private void deleteCategory(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int categoryId = parseInt(request.getParameter("id"), -1);
        if (categoryId <= 0) {
            redirectWithMessage(request, response, "Mã danh mục không hợp lệ.", true);
            return;
        }

        Category category = categoryDAO.findById(categoryId);
        if (category == null) {
            redirectWithMessage(request, response, "Không tìm thấy danh mục.", true);
            return;
        }

        if (category.getPlaceCount() > 0) {
            redirectWithMessage(request, response,
                    "Không thể xóa danh mục vì đang có " + category.getPlaceCount() + " địa điểm sử dụng danh mục này. Hãy đổi danh mục của các địa điểm trước.", true);
            return;
        }

        try {
            if (categoryDAO.delete(categoryId)) {
                logAction(request, "DELETE_CATEGORY", "Xóa danh mục #" + categoryId + " - " + category.getCategoryName());
                redirectWithMessage(request, response, "Đã xóa danh mục thành công.", false);
            } else {
                redirectWithMessage(request, response, "Không thể xóa danh mục.", true);
            }
        } catch (RuntimeException e) {
            redirectWithMessage(request, response,
                    "Không thể xóa danh mục. Danh mục có thể đang được sử dụng trong dữ liệu hệ thống.", true);
        }
    }

    private Category readForm(HttpServletRequest request) {
        Category category = new Category();
        category.setCategoryCode(trim(request.getParameter("categoryCode")));
        category.setCategoryName(trim(request.getParameter("categoryName")));
        return category;
    }

    private String validate(Category category) {
        if (category.getCategoryCode() == null || category.getCategoryCode().isEmpty()) {
            return "Mã danh mục không được để trống.";
        }
        if (category.getCategoryCode().length() > 50) {
            return "Mã danh mục không được vượt quá 50 ký tự.";
        }
        if (!category.getCategoryCode().matches("[A-Za-z0-9_-]+")) {
            return "Mã danh mục chỉ được chứa chữ cái, số, dấu gạch ngang hoặc gạch dưới.";
        }
        if (category.getCategoryName() == null || category.getCategoryName().isEmpty()) {
            return "Tên danh mục không được để trống.";
        }
        if (category.getCategoryName().length() > 100) {
            return "Tên danh mục không được vượt quá 100 ký tự.";
        }
        return null;
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void logAction(HttpServletRequest request, String action, String detail) {
        javax.servlet.http.HttpSession session = request.getSession(false);
        if (session == null) return;
        model.User user = (model.User) session.getAttribute("user");
        if (user != null) {
            auditLogDAO.log(user.getUserId(), action, null, detail);
        }
    }

    private void redirectWithMessage(HttpServletRequest request, HttpServletResponse response,
            String message, boolean error) throws IOException {
        request.getSession().setAttribute(error ? "errorMessage" : "successMessage", message);
        response.sendRedirect(request.getContextPath() + "/admin/categories");
    }
}
