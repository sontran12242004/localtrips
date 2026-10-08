<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Itinerary" scope="request"/>
<c:set var="currentTrip" value="${trip}" scope="request"/>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />

<div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
    <div>
        <h3 class="mb-1">Itinerary - ${trip.name}</h3>
        <p class="text-muted mb-0">Xem các địa điểm và hành trình dự kiến trực tiếp trên bản đồ.</p>
    </div>
    <a href="${pageContext.request.contextPath}/recommendations?tripId=${trip.id}"
       class="btn btn-outline-secondary btn-sm">← Gợi ý địa điểm</a>
</div>

<c:if test="${param.auto == 'success'}">
    <div class="alert alert-success">
        ✨ Hệ thống đã tự động thêm <strong>${param.count}</strong> địa điểm theo cấu trúc 3 cột mốc/ngày: mỗi cột mốc gồm 1 Food + 1 Cafe + 1 Tham quan/Giải trí/Mua sắm, có xét sở thích nhóm.
    </div>
</c:if>

<c:if test="${param.auto == 'empty'}">
    <div class="alert alert-warning">
        Chưa có đủ dữ liệu sở thích của các thành viên để tự động tạo lịch trình. Hãy để các thành viên chọn Preferences trước.
    </div>
</c:if>

<c:if test="${trip.role == 'OWNER'}">
    <div class="app-card mb-4 itinerary-mode-card">
        <h5 class="mb-2">Chọn cách lập lịch trình</h5>
        <div class="row g-3">
            <div class="col-md-6">
                <div class="border rounded p-3 h-100">
                    <h6>📝 Tự soạn lịch trình</h6>
                    <p class="text-muted small mb-3">Owner tự chọn địa điểm từ danh sách gợi ý và quyết định ngày, giờ, chi phí.</p>
                    <a href="${pageContext.request.contextPath}/recommendations?tripId=${trip.id}" class="btn btn-outline-secondary btn-sm">Chọn địa điểm</a>
                </div>
            </div>
            <div class="col-md-6">
                <div class="border rounded p-3 h-100">
                    <h6>✨ Hệ thống tự xếp lịch</h6>
                    <p class="text-muted small mb-3">Hệ thống tự chia mỗi ngày thành 3 cột mốc (07:00-11:00, 11:00-17:00, 17:00-22:00). Mỗi cột mốc gồm 1 điểm ăn uống, 1 điểm cà phê và 1 điểm tham quan/giải trí/mua sắm, ưu tiên theo sở thích nhóm, rating và ngân sách.</p>
                    <form method="post" action="${pageContext.request.contextPath}/itinerary/auto"
                          onsubmit="return confirm('Tự động xếp lịch theo sở thích nhóm?');">
                        <input type="hidden" name="tripId" value="${trip.id}">
                        <button type="submit" class="btn btn-brand btn-sm">Tạo lịch trình tự động</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</c:if>

<div class="app-card mb-4">
    <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <div>
            <h5 class="mb-1">🗺️ Bản đồ hành trình</h5>
            <small class="text-muted">Chọn ngày để xem các điểm của ngày đó theo đúng thứ tự lịch trình.</small>
        </div>
        <div class="d-flex align-items-center gap-2">
            <label for="mapDayFilter" class="small text-muted mb-0">Ngày:</label>
            <select id="mapDayFilter" class="form-select form-select-sm" style="min-width: 150px">
                <option value="ALL">Tất cả</option>
                <c:forEach var="item" items="${items}">
                    <option value="${item.date}">${item.date}</option>
                </c:forEach>
            </select>
            <span class="badge bg-secondary" id="mapPointCount">0 điểm</span>
        </div>
    </div>

    <div id="mapRouteInfo" class="alert alert-light border py-2 mb-3 d-none"></div>
    <div class="localtrip-map-wrap"><div id="itineraryMap" class="localtrip-map"></div></div>
    <div id="mapEmptyState" class="alert alert-light border mt-3 mb-0 d-none">
        Ngày này chưa có địa điểm nào có latitude/longitude. Hãy cập nhật tọa độ trong Quản lý Places.
    </div>
</div>

<!-- Weather by itinerary location and visit time -->
<div class="app-card mb-4" id="itineraryWeatherCard">
    <div class="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <div>
            <h5 class="mb-1">🌤️ Thời tiết theo lịch trình</h5>
            <small class="text-muted">
                Dự báo được lấy theo tọa độ địa điểm và giờ bắt đầu của từng hoạt động.
                Không cần API key.
            </small>
        </div>
        <span class="badge bg-light text-dark border" id="weatherStatus">Đang tải...</span>
    </div>

    <div id="weatherNotice" class="alert alert-light border d-none mb-3"></div>
    <div id="weatherList" class="row g-3"></div>
</div>

<style>
    .weather-card {
        border: 1px solid #e9ecef;
        border-radius: 12px;
        padding: 16px;
        height: 100%;
        background: #fff;
    }
    .weather-info-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
        padding: 8px 0;
        border-bottom: 1px solid #f1f3f5;
        font-size: 14px;
    }
    .weather-info-row:last-child {
        border-bottom: 0;
        padding-bottom: 0;
    }
    .weather-info-row span {
        color: #6c757d;
    }
    .weather-temp {
        font-size: 20px;
        font-weight: 700;
    }
</style>

<c:choose>
    <c:when test="${empty items}">
        <div class="empty-state app-card">
            <div class="empty-icon">🗓️</div>
            <p>Chưa có địa điểm nào trong lịch trình.</p>
        </div>
    </c:when>
    <c:otherwise>
        <div class="app-card p-0">
            <table class="table mb-0 align-middle">
                <thead>
                <tr>
                    <th>Địa điểm</th>
                    <th>Ngày</th>
                    <th>Giờ</th>
                    <th>Ghi chú</th>
                    <th>Chi phí dự kiến</th>
                    <c:if test="${trip.role == 'OWNER'}"><th></th></c:if>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="item" items="${items}" varStatus="status">
                    <tr>
                        <td>
                            <span class="badge bg-light text-dark border me-1">${status.index + 1}</span>
                            ${item.placeName}
                        </td>
                        <td>${item.date}</td>
                        <td>${item.startTime} - ${item.endTime}</td>
                        <td>${item.note}</td>
                        <td><fmt:formatNumber value="${item.estimatedCost}" type="number"/> đ</td>
                        <c:if test="${trip.role == 'OWNER'}">
                            <td>
                                <form method="post"
                                      action="${pageContext.request.contextPath}/itinerary/delete"
                                      onsubmit="return confirmDelete('Xóa địa điểm này khỏi lịch trình?')">
                                    <input type="hidden" name="tripId" value="${trip.id}">
                                    <input type="hidden" name="itemId" value="${item.id}">
                                    <button type="submit" class="btn btn-sm btn-outline-danger">Xóa</button>
                                </form>
                            </td>
                        </c:if>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </c:otherwise>
</c:choose>

<c:if test="${trip.role != 'OWNER'}">
    <p class="text-muted mt-2"><em>Bạn là Member: chỉ Owner mới có thể thêm/xóa mục trong itinerary.</em></p>
</c:if>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<div id="itineraryMapData" class="d-none" aria-hidden="true">
<c:forEach var="item" items="${items}">
    <c:if test="${not empty item.latitude and not empty item.longitude}">
        <span class="itinerary-map-point"
              data-id="${item.placeId}"
              data-name="<c:out value='${item.placeName}'/>"
              data-lat="${item.latitude}"
              data-lng="${item.longitude}"
              data-date="${item.date}"
              data-start="${item.startTime}"
              data-end="${item.endTime}"></span>
    </c:if>
</c:forEach>
</div>

<script>
(function () {
    const mapElement = document.getElementById('itineraryMap');
    const emptyState = document.getElementById('mapEmptyState');
    const countElement = document.getElementById('mapPointCount');
    const dayFilter = document.getElementById('mapDayFilter');
    const routeInfo = document.getElementById('mapRouteInfo');

    if (!mapElement || typeof L === 'undefined') return;

    const allPoints = Array.from(document.querySelectorAll('.itinerary-map-point')).map(function (el) {
        return {
            id: Number(el.dataset.id),
            name: el.dataset.name || '',
            lat: Number(el.dataset.lat),
            lng: Number(el.dataset.lng),
            date: el.dataset.date || '',
            start: el.dataset.start || '',
            end: el.dataset.end || ''
        };
    });

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

    let markersLayer = L.layerGroup().addTo(map);
    let fallbackLine = null;
    let routeLine = null;

    function clearMapLayers() {
        markersLayer.clearLayers();
        if (fallbackLine) {
            map.removeLayer(fallbackLine);
            fallbackLine = null;
        }
        if (routeLine) {
            map.removeLayer(routeLine);
            routeLine = null;
        }
        routeInfo.classList.add('d-none');
        routeInfo.textContent = '';
    }

    function showMapForDay(day) {
        clearMapLayers();
        emptyState.classList.add('d-none');

        const points = allPoints.filter(function (point) {
            return day === 'ALL' || point.date === day;
        });

        countElement.textContent = points.length + ' điểm';

        if (points.length === 0) {
            map.setView([10.8231, 106.6297], 11);
            emptyState.classList.remove('d-none');
            return;
        }

        const latLngs = [];
        points.forEach(function (point, index) {
            const latLng = [point.lat, point.lng];
            latLngs.push(latLng);

            const detailUrl = '${pageContext.request.contextPath}/places/detail?placeId=' + point.id;
            const popup = '<strong>' + (index + 1) + '. ' + escapeHtml(point.name) + '</strong>' +
                    '<br>' + escapeHtml(point.date) +
                    '<br>' + escapeHtml(point.start) + ' - ' + escapeHtml(point.end) +
                    '<br><a href="' + detailUrl + '">Xem chi tiết địa điểm</a>';

            const markerIcon = L.divIcon({
                className: 'localtrip-map-pin-wrap',
                html: '<div class=\"localtrip-map-pin localtrip-map-pin-number\"><span>' + (index + 1) + '</span></div>',
                iconSize: [34, 42],
                iconAnchor: [17, 40],
                popupAnchor: [0, -36]
            });
            L.marker(latLng, { icon: markerIcon }).addTo(markersLayer).bindPopup(popup);
        });

        fallbackLine = L.polyline(latLngs, {
            weight: 4,
            opacity: 0.55,
            dashArray: '8 8'
        }).addTo(map);

        map.fitBounds(L.latLngBounds(latLngs), {padding: [30, 30]});

        if (points.length < 2) return;

        const coordinates = points.map(function (point) {
            return point.lng + ',' + point.lat;
        }).join(';');

        routeInfo.textContent = 'Đang tính tuyến đường dự kiến...';
        routeInfo.classList.remove('d-none');

        fetch('https://router.project-osrm.org/route/v1/driving/' + coordinates +
              '?overview=full&geometries=geojson')
            .then(function (response) {
                if (!response.ok) throw new Error('Routing service unavailable');
                return response.json();
            })
            .then(function (data) {
                if (!data.routes || !data.routes.length) throw new Error('No route');

                const route = data.routes[0];
                routeLine = L.geoJSON(route.geometry, {
                    style: {weight: 5, opacity: 0.9, color: '#1a73e8'}
                }).addTo(map);

                if (fallbackLine) {
                    map.removeLayer(fallbackLine);
                    fallbackLine = null;
                }

                const distanceKm = (route.distance / 1000).toFixed(1);
                const durationMin = Math.round(route.duration / 60);
                const hours = Math.floor(durationMin / 60);
                const minutes = durationMin % 60;
                const durationText = hours > 0
                        ? hours + ' giờ ' + minutes + ' phút'
                        : minutes + ' phút';

                routeInfo.textContent = 'Tuyến dự kiến: ' + distanceKm +
                        ' km · thời gian lái xe khoảng ' + durationText + '.';
                map.fitBounds(routeLine.getBounds(), {padding: [30, 30]});
            })
            .catch(function () {
                routeInfo.textContent = 'Không lấy được tuyến đường thực tế. Bản đồ đang hiển thị đường nối dự kiến giữa các điểm.';
            });
    }

    function escapeHtml(value) {
        return String(value)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
    }

    dayFilter.addEventListener('change', function () {
        showMapForDay(this.value);
    });

    showMapForDay('ALL');
})();
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>

<script>
(function () {
    const list = document.getElementById('weatherList');
    const status = document.getElementById('weatherStatus');
    const notice = document.getElementById('weatherNotice');

    if (!list || !status || !notice) return;

    // Lấy thông tin ngày, giờ và tọa độ từ các điểm đã có trên lịch trình.
    const points = Array.from(document.querySelectorAll('.itinerary-map-point')).map(function (el) {
        return {
            name: el.dataset.name || 'Địa điểm',
            lat: Number(el.dataset.lat),
            lng: Number(el.dataset.lng),
            date: el.dataset.date || '',
            start: el.dataset.start || '',
            end: el.dataset.end || ''
        };
    }).filter(function (p) {
        return Number.isFinite(p.lat) && Number.isFinite(p.lng) && p.date && p.start;
    });

    function escapeHtml(value) {
        return String(value == null ? '' : value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function hourKey(start) {
        const match = String(start).match(/^(\d{1,2}):(\d{2})/);
        if (!match) return null;
        return String(Number(match[1])).padStart(2, '0') + ':' + match[2];
    }

    function dateDiffDays(dateString) {
        const target = new Date(dateString + 'T00:00:00');
        const now = new Date();
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        return Math.floor((target - today) / 86400000);
    }

    function addNotice(message) {
        notice.classList.remove('d-none');
        notice.innerHTML = message;
    }

    function createCard(point, data) {
        const temp = data.temperature_2m;
        const rainProbability = data.precipitation_probability;

        return '<div class="col-md-6 col-xl-4">' +
            '<div class="weather-card">' +
                '<div class="fw-semibold mb-3">📍 ' + escapeHtml(point.name) + '</div>' +
                '<div class="weather-info-row">' +
                    '<span>📅 Ngày đi</span>' +
                    '<strong>' + escapeHtml(point.date) + '</strong>' +
                '</div>' +
                '<div class="weather-info-row">' +
                    '<span>🕐 Khung giờ</span>' +
                    '<strong>' + escapeHtml(point.start) + ' - ' + escapeHtml(point.end) + '</strong>' +
                '</div>' +
                '<div class="weather-info-row">' +
                    '<span>🌡️ Nhiệt độ</span>' +
                    '<strong class="weather-temp">' +
                        (temp == null ? '--' : Number(temp).toFixed(0) + '°C') +
                    '</strong>' +
                '</div>' +
                '<div class="weather-info-row">' +
                    '<span>🌧️ Khả năng mưa</span>' +
                    '<strong>' +
                        (rainProbability == null ? '--' : Number(rainProbability) + '%') +
                    '</strong>' +
                '</div>' +
            '</div>' +
        '</div>';
    }

    async function fetchWeather(point) {
        const hour = hourKey(point.start);
        if (!hour) throw new Error('Giờ bắt đầu không hợp lệ.');

        const diff = dateDiffDays(point.date);

        // Chỉ hiển thị khi Open-Meteo có thể cung cấp dự báo.
        if (diff < 0 || diff > 16) {
            return {
                unavailable: true,
                reason: diff < 0
                    ? 'Ngày này đã qua.'
                    : 'Ngày đi còn quá xa nên chưa có dự báo thời tiết.'
            };
        }

        const url =
            'https://api.open-meteo.com/v1/forecast' +
            '?latitude=' + encodeURIComponent(point.lat) +
            '&longitude=' + encodeURIComponent(point.lng) +
            '&hourly=temperature_2m,precipitation_probability' +
            '&timezone=Asia%2FHo_Chi_Minh' +
            '&start_date=' + encodeURIComponent(point.date) +
            '&end_date=' + encodeURIComponent(point.date);

        const response = await fetch(url);
        if (!response.ok) {
            throw new Error('Không lấy được dữ liệu thời tiết.');
        }

        const data = await response.json();
        const times = data.hourly && data.hourly.time ? data.hourly.time : [];
        const index = times.findIndex(function (value) {
            return value.substring(11, 16) === hour;
        });

        if (index < 0) {
            throw new Error('Không tìm thấy dữ liệu theo giờ.');
        }

        return {
            temperature_2m: data.hourly.temperature_2m[index],
            precipitation_probability: data.hourly.precipitation_probability[index]
        };
    }

    async function loadWeather() {
        if (points.length === 0) {
            status.textContent = 'Chưa có dữ liệu';
            addNotice('Chưa có địa điểm trong lịch trình có đủ <strong>tọa độ + ngày + giờ</strong> để hiển thị thời tiết.');
            return;
        }

        status.textContent = 'Đang tải...';

        let successCount = 0;
        let unavailableCount = 0;
        const fragments = [];

        for (const point of points) {
            try {
                const data = await fetchWeather(point);

                if (data.unavailable) {
                    unavailableCount++;
                    continue;
                }

                fragments.push(createCard(point, data));
                successCount++;
            } catch (error) {
                console.warn('Weather error for', point.name, error);
            }
        }

        list.innerHTML = fragments.join('');

        if (successCount > 0) {
            status.textContent = successCount + ' hoạt động có dự báo';
            if (unavailableCount > 0) {
                addNotice('Một số hoạt động chưa có dự báo. Khi ngày đi nằm trong phạm vi dự báo, hệ thống sẽ tự động hiển thị nhiệt độ và khả năng mưa.');
            }
        } else {
            status.textContent = 'Chưa có dự báo';
            addNotice('Hiện chưa có dự báo thời tiết cho các hoạt động trong lịch trình.');
        }
    }

    loadWeather();
})();
</script>
