package controller.auth;

import java.io.IOException;

import dao.UserDAO;
import utils.PasswordUtil;
import utils.ValidationUtil;
import utils.PasswordValidator;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dang ky tai khoan. Cung nguyen tac voi LoginServlet: loi thi FORWARD ve lai
 * register.jsp kem errorMessage, khong redirect - de giu lai du lieu nguoi dung
 * da nhap.
 *
 * DIEU KIEN THEM USER MOI (kiem tra o SERVER, khong chi dua vao
 * required/minlength cua HTML vi nguoi dung co the tat JS hoac goi thang
 * request bang Postman): 1. Khong duoc de trong: ho ten, email, mat khau, nhap
 * lai mat khau 2. Mat khau va nhap lai mat khau phai khop nhau 3. Ho ten toi da
 * 100 ky tu (dung do dai cot Users.full_name) 4. Email dung dinh dang, toi da
 * 150 ky tu (dung do dai cot Users.email) 5. Mat khau tu 6 den 64 ky tu 6.
 * Email chua ton tai trong he thong (kiem tra truoc bang SELECT) 7. Xu ly them
 * truong hop 2 nguoi dang ky CUNG 1 email CUNG LUC (race condition vuot qua
 * buoc 6): dua vao UNIQUE constraint that su cua DB, UserDAO.insert() tra ve
 * false neu trung thay vi nem loi 500.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final String REGISTER_VIEW = "/WEB-INF/views/auth/register.jsp";

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // ---- Dieu kien 1: khong duoc de trong ----
        if (ValidationUtil.isBlank(fullName) || ValidationUtil.isBlank(email)
                || ValidationUtil.isBlank(password) || ValidationUtil.isBlank(confirmPassword)) {
            forward(request, response, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // ---- Dieu kien 3: do dai ho ten ----
        if (!ValidationUtil.isValidName(fullName)) {
            forward(request, response, "Họ tên không được vượt quá "
                    + ValidationUtil.NAME_MAX + " ký tự.");
            return;
        }

        // ---- Dieu kien 4: dinh dang + do dai email ----
        String cleanEmail = email.trim().toLowerCase();
        if (!ValidationUtil.isValidEmail(cleanEmail)) {
            forward(request, response, "Email không đúng định dạng.");
            return;
        }

        // ---- Dieu kien 5: do dai mat khau ----
        String passwordError = PasswordValidator.validate(password);

        if (passwordError != null) {
            forward(request, response, passwordError);
            return;
        }

        // ---- Dieu kien 2: khop mat khau nhap lai ----
        if (!password.equals(confirmPassword)) {
            forward(request, response, "Mật khẩu nhập lại không khớp.");
            return;
        }

        // ---- Dieu kien 6: email chua ton tai ----
        boolean created;
        try {
            if (userDAO.existsByEmail(cleanEmail)) {
                forward(request, response, "Email này đã được sử dụng.");
                return;
            }
            // Dieu kien 8: UserDAO.insert tra ve false neu 2 nguoi dang ky
            // cung email cung luc (loi UNIQUE that su tu DB, khong phai
            // check phia Java) - van bao loi ro rang, khong van 500.
            created = userDAO.insert(fullName.trim(), cleanEmail, PasswordUtil.hash(password));
        } catch (Exception e) {
            forward(request, response, "Không thể kết nối hệ thống, vui lòng thử lại.");
            return;
        }

        if (!created) {
            forward(request, response, "Email này đã được sử dụng.");
            return;
        }

        request.getSession(true).setAttribute("successMessage",
                "Đăng ký thành công! Vui lòng đăng nhập.");
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String errorMessage)
            throws ServletException, IOException {
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher(REGISTER_VIEW);
        dispatcher.forward(request, response);
    }
}
