// Xác nhận trước khi xóa (itinerary item, expense...)
function confirmDelete(message) {
    return confirm(message || "Bạn có chắc muốn xóa mục này?");
}

// Cập nhật giao diện sau khi trang tải xong
document.addEventListener("DOMContentLoaded", function () {
    // Đánh dấu liên kết trang hiện tại
    document.querySelectorAll(".app-nav-links a, .trip-workspace a").forEach(function (link) {
        var url = new URL(link.href, location.href);

        if (url.pathname === location.pathname) {
            link.classList.add("active");
            link.setAttribute("aria-current", "page");
        }
    });

    // Cho phép cuộn ngang bảng bằng bàn phím
    document.querySelectorAll("main table").forEach(function (table) {
        var wrapper = table.closest(".table-responsive");

        if (!wrapper) {
            wrapper = document.createElement("div");
            wrapper.className = "table-responsive";
            table.parentNode.insertBefore(wrapper, table);
            wrapper.appendChild(table);
        }

        wrapper.tabIndex = 0;
        wrapper.setAttribute("role", "region");
        wrapper.setAttribute(
                "aria-label",
                "Bảng dữ liệu — cuộn ngang để xem thêm"
                );
    });

    // Cập nhật số lượng category đã chọn trong preference-form.jsp
    var checkboxes = document.querySelectorAll(".preference-checkbox");
    var counter = document.getElementById("selectedCount");

    if (checkboxes.length && counter) {
        var updateCount = function () {
            var checked = document.querySelectorAll(
                    ".preference-checkbox:checked"
                    ).length;

            counter.textContent = checked;
        };

        checkboxes.forEach(function (checkbox) {
            checkbox.addEventListener("change", updateCount);
        });

        updateCount();
    }

    // Chọn hoặc bỏ chọn tất cả người tham gia expense
    var selectAll = document.getElementById("selectAllParticipants");

    if (selectAll) {
        selectAll.addEventListener("change", function () {
            document.querySelectorAll(".participant-checkbox").forEach(function (checkbox) {
                checkbox.checked = selectAll.checked;
            });
        });
    }

    // Khi chi từ quỹ nhóm, mặc định người ghi nhận là Owner
    // Khi chọn chi từ quỹ nhóm, mặc định Owner là người trả
    var fromFund = document.getElementById("fromFund");
    var payerSelect = document.getElementById("payerId");

    if (fromFund && payerSelect) {
        fromFund.addEventListener("change", function () {
            if (!fromFund.checked) {
                return;
            }

            var ownerOption = payerSelect.querySelector(
                    'option[data-owner="true"]'
                    );

            if (ownerOption) {
                payerSelect.value = ownerOption.value;
            }
        });
    }
}); 