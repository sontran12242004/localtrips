<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Places" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<div class="page-intro"><span class="eyebrow">ĐI &amp; KHÁM PHÁ</span><h3>Điểm đến cho hành trình tiếp theo</h3><p class="text-muted">Tìm địa điểm theo sở thích, xem thông tin và lên kế hoạch cùng nhóm.</p></div>

<link rel="stylesheet"
      href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />

<div class="app-card mb-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
            <h5 class="mb-1">🗺️ Bản đồ địa điểm</h5>
            <small class="text-muted">Hiển thị các địa điểm đang hoạt động có tọa độ.</small>
        </div>
        <span class="badge bg-secondary" id="placeMapCount">0 điểm</span>
    </div>
    <div class="localtrip-map-wrap"><div id="placeMap" class="localtrip-map"></div></div>
    <div id="placeMapEmpty" class="alert alert-light border mt-3 mb-0 d-none">
        Chưa có địa điểm nào có latitude/longitude để hiển thị.
    </div>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<div id="placeMapData" class="d-none" aria-hidden="true">
<c:forEach var="place" items="${places}">
    <c:if test="${not empty place.latitude and not empty place.longitude}">
        <span class="place-map-point"
              data-id="${place.id}"
              data-name="<c:out value='${place.name}'/>"
              data-address="<c:out value='${place.address}'/>"
              data-category="<c:out value='${place.categoryName}'/>"
              data-lat="${place.latitude}"
              data-lng="${place.longitude}"></span>
    </c:if>
</c:forEach>
</div>

<script>
(function () {
    const mapElement = document.getElementById('placeMap');
    const emptyState = document.getElementById('placeMapEmpty');
    const countElement = document.getElementById('placeMapCount');
    if (!mapElement || typeof L === 'undefined') return;

    const points = Array.from(document.querySelectorAll('.place-map-point')).map(function (el) {
        return {
            id: Number(el.dataset.id),
            name: el.dataset.name || '',
            address: el.dataset.address || '',
            category: el.dataset.category || '',
            lat: Number(el.dataset.lat),
            lng: Number(el.dataset.lng)
        };
    });

    countElement.textContent = points.length + ' điểm';
    const map = L.map(mapElement).setView([10.8231, 106.6297], 11);
    const tileStatus = document.createElement('div');
    tileStatus.className = 'map-tile-status';
    tileStatus.textContent = 'Đang tải bản đồ...';
    mapElement.parentElement.appendChild(tileStatus);

    let activeTiles = null;
    let tileProviderIndex = 0;
    const tileProviders = [
        {
            name: 'Esri World Street Map',
            url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}',
            options: { maxZoom: 19, attribution: 'Tiles &copy; Esri' }
        },
        {
            name: 'OpenStreetMap',
            url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
            options: { maxZoom: 19, attribution: '&copy; OpenStreetMap contributors' }
        },
        {
            name: 'CARTO Voyager',
            url: 'https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png',
            options: { maxZoom: 19, subdomains: 'abcd', attribution: '&copy; OpenStreetMap contributors &copy; CARTO' }
        }
    ];

    function loadTileProvider(index) {
        if (index >= tileProviders.length) {
            tileStatus.textContent = 'Không thể tải dữ liệu bản đồ. Hãy kiểm tra kết nối Internet.';
            tileStatus.classList.add('map-tile-status-error');
            return;
        }
        tileProviderIndex = index;
        const provider = tileProviders[index];
        if (activeTiles) map.removeLayer(activeTiles);
        tileStatus.textContent = 'Đang tải bản đồ ' + provider.name + '...';
        activeTiles = L.tileLayer(provider.url, provider.options);
        let hadTileError = false;
        activeTiles.on('tileerror', function () {
            if (hadTileError) return;
            hadTileError = true;
            loadTileProvider(index + 1);
        });
        activeTiles.on('load', function () {
            tileStatus.textContent = 'Bản đồ: ' + provider.name;
            setTimeout(function () { if (tileStatus.parentNode) tileStatus.remove(); }, 2200);
        });
        activeTiles.addTo(map);
    }

    loadTileProvider(0);
    L.control.scale({ imperial: false }).addTo(map);

    if (points.length === 0) {
        emptyState.classList.remove('d-none');
        return;
    }

    const bounds = [];
    points.forEach(function (point) {
        const latLng = [point.lat, point.lng];
        bounds.push(latLng);
        const detailUrl = '${pageContext.request.contextPath}/places/detail?placeId=' + point.id;
        const markerIcon = L.divIcon({
            className: 'localtrip-map-pin-wrap',
            html: '<div class=\"localtrip-map-pin\"><span>●</span></div>',
            iconSize: [34, 42],
            iconAnchor: [17, 40],
            popupAnchor: [0, -36]
        });
        L.marker(latLng, { icon: markerIcon }).addTo(map).bindPopup(
            '<strong>' + escapeHtml(point.name) + '</strong><br>' +
            escapeHtml(point.category) + '<br>' +
            escapeHtml(point.address) + '<br>' +
            '<a href="' + detailUrl + '">Xem chi tiết địa điểm</a>'
        );
    });

    map.fitBounds(L.latLngBounds(bounds), {padding: [30, 30]});

    function escapeHtml(value) {
        return String(value)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
    }
})();
</script>

<div class="mb-4">
    <a href="${pageContext.request.contextPath}/places"
       class="category-chip ${empty param.categoryId ? 'active' : ''}">Tất cả</a>
    <c:forEach var="category" items="${categories}">
        <a href="${pageContext.request.contextPath}/places?categoryId=${category.id}"
           class="category-chip ${param.categoryId == category.id ? 'active' : ''}">
            ${category.icon} ${category.name}
        </a>
    </c:forEach>
</div>

<c:choose>
    <c:when test="${empty places}">
        <div class="empty-state app-card">
            <div class="empty-icon">📍</div>
            <p>Không có địa điểm nào trong danh mục này.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="row">
            <c:forEach var="place" items="${places}">
                <div class="col-md-6 col-lg-4">
                    <div class="app-card h-100">
                        <div class="d-flex justify-content-between">
                            <h5>${place.name}</h5>
                            <span class="badge bg-secondary">⭐ ${place.rating}</span>
                        </div>
                        <p class="text-muted mb-1">${place.categoryIcon} ${place.categoryName}</p>
                        <p class="text-muted mb-1">📍 ${place.address}</p>
                        <p class="text-muted mb-1">🕒 ${place.openHours}</p>
                        <p class="mb-3">💰 ~${place.estimatedPrice} đ</p>
                        <a href="${pageContext.request.contextPath}/places/detail?placeId=${place.id}"
                           class="btn btn-outline-secondary btn-sm w-100">Xem chi tiết</a>
                    </div>
                </div>
            </c:forEach>
        </div>
    </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
