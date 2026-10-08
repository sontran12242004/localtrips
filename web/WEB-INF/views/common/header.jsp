<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="model.User,utils.HtmlUtil" %>

<%
    String pageTitleText
            = (String) request.getAttribute("pageTitle");

    User headerUser
            = (User) session.getAttribute("user");

    boolean headerIsAdmin
            = headerUser != null
            && "ADMIN".equalsIgnoreCase(
                    headerUser.getRole()
            );

    boolean headerIsUser
            = headerUser != null
            && "USER".equalsIgnoreCase(
                    headerUser.getRole()
            );

    String headerHomeUrl
            = request.getContextPath() + "/login";

    if (headerIsAdmin) {
        headerHomeUrl
                = request.getContextPath() + "/admin";
    } else if (headerIsUser) {
        headerHomeUrl
                = request.getContextPath() + "/user";
    }

    String headerError
            = (String) request.getAttribute("errorMessage");

    String headerSuccess
            = (String) request.getAttribute("successMessage");

    String sessionError
            = (String) session.getAttribute("errorMessage");

    String sessionSuccess
            = (String) session.getAttribute("successMessage");

    if (headerError == null) {
        headerError = sessionError;
    }

    if (headerSuccess == null) {
        headerSuccess = sessionSuccess;
    }

    session.removeAttribute("errorMessage");
    session.removeAttribute("successMessage");
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>
        <%= HtmlUtil.escape(
                pageTitleText == null
                ? "LocalTrip"
                : pageTitleText + " - LocalTrip"
        ) %>
    </title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">

    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/css/style.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/localtrip.css?v=1">
</head>

<body class="<%= headerUser == null ? "auth-page" : (headerIsAdmin ? "admin-page" : "user-page") %>">
<a class="skip-link" href="#main-content">Đi đến nội dung</a>

<nav class="navbar navbar-expand-lg app-navbar sticky-top">
    <div class="container">

        <a class="navbar-brand app-brand"
           href="<%= headerHomeUrl %>">

            <span class="app-brand-icon">L</span>

            <span>
                Local<span>Trip</span>
            </span>
        </a>

        <button class="navbar-toggler"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#mainNavigation"
                aria-controls="mainNavigation"
                aria-expanded="false"
                aria-label="Mở menu">

            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse"
             id="mainNavigation">

            <% if (headerIsUser) { %>

                <ul class="navbar-nav me-auto app-nav-links">

                    <li class="nav-item">
                        <a class="nav-link"
                           href="<%= request.getContextPath() %>/user">
                            Tổng quan
                        </a>
                    </li>

                    <li class="nav-item">
                        <a class="nav-link"
                           href="<%= request.getContextPath() %>/trips">
                            Chuyến đi
                        </a>
                    </li>

                    <li class="nav-item">
                        <a class="nav-link"
                           href="<%= request.getContextPath() %>/places">
                            Địa điểm
                        </a>
                    </li>
                </ul>

            <% } else if (headerIsAdmin) { %>

                <ul class="navbar-nav me-auto app-nav-links">

                    <li class="nav-item">
                        <a class="nav-link"
                           href="<%= request.getContextPath() %>/admin">
                            Tổng quan
                        </a>
                    </li>

                    <li class="nav-item">
                        <a class="nav-link"
                           href="<%= request.getContextPath() %>/admin/users">
                            Người dùng
                        </a>
                    </li>

                    <li class="nav-item">
                        <a class="nav-link"
                           href="<%= request.getContextPath() %>/admin/places">
                            Địa điểm
                        </a>
                    </li>

                    <li class="nav-item">
                        <a class="nav-link"
                           href="<%= request.getContextPath() %>/admin/audit-logs">
                            Nhật ký
                        </a>
                    </li>
                </ul>

            <% } %>

            <% if (headerUser != null) { %>

                <div class="app-user-menu ms-lg-auto">

                    <div class="app-avatar">
                        <%= HtmlUtil.escape(
                                headerUser.getFullName()
                                        .substring(0, 1)
                                        .toUpperCase()
                        ) %>
                    </div>

                    <div class="app-user-info">
                        <span class="app-user-name">
                            <%= HtmlUtil.escape(
                                    headerUser.getFullName()
                            ) %>
                        </span>

                        <span class="app-user-role">
                            <%= HtmlUtil.escape(
                                    headerUser.getRole()
                            ) %>
                        </span>
                    </div>

                    <a href="<%= request.getContextPath() %>/logout"
                       class="btn app-logout-btn">
                        Đăng xuất
                    </a>
                </div>

            <% } else { %>

                <div class="ms-lg-auto d-flex gap-2">

                    <a href="<%= request.getContextPath() %>/login"
                       class="btn btn-outline-light">
                        Đăng nhập
                    </a>

                    <a href="<%= request.getContextPath() %>/register"
                       class="btn btn-light">
                        Đăng ký
                    </a>
                </div>

            <% } %>
        </div>
    </div>
</nav>

<main class="container app-main" id="main-content" tabindex="-1">
<%
    Object workspaceTripObject = request.getAttribute("currentTrip");
    if (workspaceTripObject == null) workspaceTripObject = request.getAttribute("trip");
    if (headerIsUser && workspaceTripObject instanceof model.Trip) {
        model.Trip workspaceTrip = (model.Trip) workspaceTripObject;
        if (workspaceTrip.getTripId() > 0) {
%>
<div class="trip-workspace">
    <div class="trip-workspace-label">Không gian chuyến đi · <%= HtmlUtil.escape(workspaceTrip.getTripName()) %></div>
    <nav aria-label="Các chức năng chuyến đi">
        <a href="<%= request.getContextPath() %>/trip/detail?tripId=<%= workspaceTrip.getTripId() %>">Tổng quan</a>
        <a href="<%= request.getContextPath() %>/preferences?tripId=<%= workspaceTrip.getTripId() %>">Sở thích</a>
        <a href="<%= request.getContextPath() %>/preferences/group?tripId=<%= workspaceTrip.getTripId() %>">Sở thích nhóm</a>
        <a href="<%= request.getContextPath() %>/recommendations?tripId=<%= workspaceTrip.getTripId() %>">Gợi ý địa điểm</a>
        <a href="<%= request.getContextPath() %>/itinerary?tripId=<%= workspaceTrip.getTripId() %>">Lịch trình</a>
        <a href="<%= request.getContextPath() %>/expenses?tripId=<%= workspaceTrip.getTripId() %>">Chi phí</a>
        <a href="<%= request.getContextPath() %>/settlement?tripId=<%= workspaceTrip.getTripId() %>">Chia tiền</a>
        <a href="<%= request.getContextPath() %>/group-fund?tripId=<%= workspaceTrip.getTripId() %>">Quỹ nhóm</a>
    </nav>
</div>
<% } } %>

    <% if (headerError != null
            && !headerError.isEmpty()) { %>

        <div class="app-alert app-alert-error">
            <span class="app-alert-icon">!</span>

            <span>
                <%= HtmlUtil.escape(headerError) %>
            </span>
        </div>

    <% } %>

    <% if (headerSuccess != null
            && !headerSuccess.isEmpty()) { %>

        <div class="app-alert app-alert-success">
            <span class="app-alert-icon">✓</span>

            <span>
                <%= HtmlUtil.escape(headerSuccess) %>
            </span>
        </div>

    <% } %>

