<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="Model.Bean.User" %>
<%@ page import="Model.Bean.Task" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
    
    List<Task> taskHistory = (List<Task>) request.getAttribute("taskHistory");
    Integer pendingCount = (Integer) request.getAttribute("pendingCount");
    Integer processingCount = (Integer) request.getAttribute("processingCount");
    Integer completedCount = (Integer) request.getAttribute("completedCount");
    Integer failedCount = (Integer) request.getAttribute("failedCount");
    Integer totalCount = (Integer) request.getAttribute("totalCount");
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch Sử Tasks - Speech to Text</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: Arial, sans-serif;
            background: #f5f5f5;
            min-height: 100vh;
            padding: 15px;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        
        .header {
            background: #4a5568;
            color: white;
            padding: 15px 20px;
            border-radius: 8px 8px 0 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .header h1 {
            font-size: 20px;
        }
        
        .user-info {
            display: flex;
            align-items: center;
            gap: 15px;
            font-size: 13px;
        }
        
        .nav-links {
            display: flex;
            gap: 8px;
        }
        
        .btn {
            padding: 6px 14px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 13px;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn-primary {
            background: white;
            color: #4a5568;
        }
        
        .btn-primary:hover {
            background: #e2e8f0;
        }
        
        .btn-secondary {
            background: #2d3748;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #1a202c;
        }
        
        .main-content {
            background: white;
            padding: 25px;
            border-radius: 0 0 8px 8px;
            border: 1px solid #e0e0e0;
            border-top: none;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
            gap: 15px;
            margin-bottom: 25px;
        }
        
        .stat-card {
            padding: 20px;
            border-radius: 8px;
            text-align: center;
            border: 1px solid #e0e0e0;
            background: white;
        }
        
        .stat-card.total {
            background: #4a5568;
            color: white;
            border-color: #4a5568;
        }
        
        .stat-card.pending {
            background: #fff3cd;
            color: #856404;
            border-color: #ffeaa7;
        }
        
        .stat-card.processing {
            background: #d1ecf1;
            color: #0c5460;
            border-color: #bee5eb;
        }
        
        .stat-card.completed {
            background: #d4edda;
            color: #155724;
            border-color: #c3e6cb;
        }
        
        .stat-card.failed {
            background: #f8d7da;
            color: #721c24;
            border-color: #f5c6cb;
        }
        
        .stat-card h3 {
            font-size: 32px;
            margin-bottom: 8px;
            font-weight: bold;
        }
        
        .stat-card p {
            font-size: 13px;
        }
        
        .task-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 15px;
            table-layout: fixed;
            border: 1px solid #e0e0e0;
        }
        
        .task-table th {
            background: #f8f9fa;
            padding: 12px 8px;
            text-align: left;
            font-weight: bold;
            color: #333;
            border-bottom: 2px solid #dee2e6;
            font-size: 13px;
        }
        
        .task-table th:nth-child(1) { width: 6%; }  /* ID */
        .task-table th:nth-child(2) { width: 24%; } /* Tên File */
        .task-table th:nth-child(3) { width: 12%; } /* Trạng Thái */
        .task-table th:nth-child(4) { width: 14%; } /* Thời Gian Gửi */
        .task-table th:nth-child(5) { width: 14%; } /* Thời Gian Hoàn Thành */
        .task-table th:nth-child(6) { width: 10%; } /* Thời Gian Xử Lý */
        .task-table th:nth-child(7) { width: 20%; } /* Thao Tác */
        
        .task-table td {
            padding: 12px 8px;
            border-bottom: 1px solid #e0e0e0;
            word-wrap: break-word;
            overflow-wrap: break-word;
            font-size: 13px;
        }
        
        .task-table td.filename-cell {
            max-width: 300px;
            white-space: normal;
            word-break: break-word;
        }
        
        .task-table tr:hover {
            background: #f8f9fa;
        }
        
        .status-badge {
            padding: 4px 12px;
            border-radius: 4px;
            font-size: 12px;
            font-weight: bold;
            display: inline-block;
        }
        
        .status-pending {
            background: #fff3cd;
            color: #856404;
            border: 1px solid #ffeaa7;
        }
        
        .status-processing {
            background: #d1ecf1;
            color: #0c5460;
            border: 1px solid #bee5eb;
        }
        
        .status-completed {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        
        .status-failed {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        
        .btn-view {
            padding: 6px 10px;
            background: #4a5568;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            text-decoration: none;
            display: inline-block;
            margin-right: 4px;
            line-height: 1;
            transition: all 0.2s;
        }
        
        .btn-view:hover {
            background: #2d3748;
            transform: translateY(-1px);
        }
        
        .btn-delete {
            padding: 6px 10px;
            background: #dc3545;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            text-decoration: none;
            display: inline-block;
            line-height: 1;
            transition: all 0.2s;
        }
        
        .btn-delete:hover {
            background: #c82333;
            transform: translateY(-1px);
        }
        
        .btn-delete:disabled {
            background: #ccc;
            cursor: not-allowed;
            transform: none;
        }
        
        .btn-download {
            padding: 6px 10px;
            background: #28a745;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            text-decoration: none;
            display: inline-block;
            margin-right: 4px;
            position: relative;
            line-height: 1;
            transition: all 0.2s;
        }
        
        .btn-download:hover {
            background: #218838;
            transform: translateY(-1px);
        }
        
        /* Dropdown menu cho tải xuống */
        .dropdown {
            position: relative;
            display: inline-block;
            vertical-align: middle;
        }
        
        /* Tạo vùng invisible để chuột có thể di chuyển qua menu */
        .dropdown::before {
            content: '';
            position: absolute;
            bottom: 100%;
            left: 0;
            right: 0;
            height: 15px;
            z-index: 999;
        }
        
        .dropdown-content {
            display: none;
            position: absolute;
            background-color: white;
            min-width: 130px;
            box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);
            z-index: 1000;
            border-radius: 6px;
            overflow: hidden;
            bottom: 100%;
            left: 50%;
            transform: translateX(-50%);
            margin-bottom: 8px;
            border: 1px solid #e0e0e0;
        }
        
        .dropdown-content::after {
            content: '';
            position: absolute;
            top: 100%;
            left: 50%;
            margin-left: -5px;
            border-width: 5px;
            border-style: solid;
            border-color: white transparent transparent transparent;
        }
        
        .dropdown-content a {
            color: #333;
            padding: 10px 15px;
            text-decoration: none;
            display: block;
            font-size: 12px;
            border-bottom: 1px solid #f0f0f0;
            transition: background 0.2s;
        }
        
        .dropdown-content a:last-child {
            border-bottom: none;
        }
        
        .dropdown-content a:hover {
            background-color: #f8f9fa;
        }
        
        .dropdown:hover .dropdown-content,
        .dropdown-content:hover {
            display: block;
            animation: fadeIn 0.2s;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; transform: translateX(-50%) translateY(-5px); }
            to { opacity: 1; transform: translateX(-50%) translateY(0); }
        }
        
        .action-buttons {
            white-space: nowrap;
            display: flex;
            gap: 4px;
            align-items: center;
            justify-content: flex-start;
        }
        
        /* Tooltip cho icon buttons */
        .btn-view, .btn-delete, .btn-download {
            position: relative;
        }
        
        .btn-view::before,
        .btn-delete::before,
        .btn-download::before {
            content: attr(data-tooltip);
            position: absolute;
            bottom: 100%;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0,0,0,0.8);
            color: white;
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 11px;
            white-space: nowrap;
            opacity: 0;
            pointer-events: none;
            transition: opacity 0.2s;
            margin-bottom: 5px;
        }
        
        .btn-view:hover::before,
        .btn-delete:hover::before,
        .btn-download:hover::before {
            opacity: 1;
        }
        
        .download-group {
            margin-top: 15px;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 6px;
            border: 1px solid #e0e0e0;
        }
        
        .download-group h3 {
            font-size: 14px;
            margin-bottom: 10px;
            color: #333;
        }
        
        .empty-state {
            text-align: center;
            padding: 50px 20px;
            color: #666;
        }
        
        .empty-state-icon {
            font-size: 48px;
            margin-bottom: 15px;
        }
        
        .empty-state h3 {
            font-size: 18px;
            margin-bottom: 8px;
        }
        
        .empty-state p {
            font-size: 13px;
        }
        
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.5);
        }
        
        .modal-content {
            background: white;
            margin: 5% auto;
            padding: 25px;
            border-radius: 8px;
            border: 1px solid #e0e0e0;
            width: 80%;
            max-width: 800px;
            max-height: 80vh;
            overflow-y: auto;
        }
        
        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 12px;
            border-bottom: 2px solid #e0e0e0;
        }
        
        .modal-header h2 {
            font-size: 18px;
        }
        
        .close {
            font-size: 24px;
            font-weight: bold;
            cursor: pointer;
            color: #666;
        }
        
        .close:hover {
            color: #000;
        }
        
        .detail-row {
            padding: 12px 0;
            border-bottom: 1px solid #e0e0e0;
        }
        
        .detail-row label {
            font-weight: bold;
            color: #333;
            display: block;
            margin-bottom: 5px;
            font-size: 13px;
        }
        
        .detail-row .value {
            color: #666;
            font-size: 13px;
        }
        
        .result-text {
            background: #f8f9fa;
            padding: 12px;
            border-radius: 4px;
            white-space: pre-wrap;
            word-wrap: break-word;
            max-height: 400px;
            overflow-y: auto;
            font-family: Arial, sans-serif;
            font-size: 13px;
            line-height: 1.6;
            border: 1px solid #e0e0e0;
            color: #333;
        }
        
        .refresh-btn {
            float: right;
            margin-bottom: 15px;
        }
        
        @media (max-width: 768px) {
            .header {
                flex-direction: column;
                gap: 10px;
            }
            
            .user-info {
                flex-direction: column;
                gap: 8px;
            }
            
            .stats-grid {
                grid-template-columns: 1fr;
            }
            
            .task-table {
                font-size: 12px;
            }
            
            .task-table th,
            .task-table td {
                padding: 8px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>📊 Lịch Sử Tasks</h1>
            <div class="user-info">
                <span>👤 <%= user.getUsername() %></span>
                <div class="nav-links">
                    <a href="<%= request.getContextPath() %>/home" class="btn btn-secondary">Home</a>
                    <a href="<%= request.getContextPath() %>/upload" class="btn btn-primary">Upload</a>
                    <a href="<%= request.getContextPath() %>/logout" class="btn btn-secondary">Đăng xuất</a>
                </div>
            </div>
        </div>
        
        <div class="main-content">
            <div class="stats-grid">
                <div class="stat-card total">
                    <h3><%= totalCount != null ? totalCount : 0 %></h3>
                    <p>📝 Tổng Tasks</p>
                </div>
                <div class="stat-card pending">
                    <h3><%= pendingCount != null ? pendingCount : 0 %></h3>
                    <p>⏳ Đang chờ</p>
                </div>
                <div class="stat-card processing">
                    <h3><%= processingCount != null ? processingCount : 0 %></h3>
                    <p>🔄 Đang xử lý</p>
                </div>
                <div class="stat-card completed">
                    <h3><%= completedCount != null ? completedCount : 0 %></h3>
                    <p>✅ Hoàn thành</p>
                </div>
                <div class="stat-card failed">
                    <h3><%= failedCount != null ? failedCount : 0 %></h3>
                    <p>❌ Thất bại</p>
                </div>
            </div>
            
            <button class="btn btn-primary refresh-btn" onclick="location.reload()">🔄 Làm mới</button>
            
            <h2 style="margin-top: 20px; margin-bottom: 15px;">Danh Sách Tasks</h2>
            
            <% if (taskHistory != null && !taskHistory.isEmpty()) { %>
                <table class="task-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tên File</th>
                            <th>Trạng Thái</th>
                            <th>Thời Gian Gửi</th>
                            <th>Thời Gian Hoàn Thành</th>
                            <th>Thời Gian Xử Lý</th>
                            <th>Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Task task : taskHistory) { 
                            String statusClass = "status-" + task.getStatus().toLowerCase();
                            String statusText = task.getStatus();
                            String statusIcon = "";
                            
                            switch (task.getStatus()) {
                                case "PENDING": statusIcon = "⏳"; statusText = "Đang chờ"; break;
                                case "PROCESSING": statusIcon = "🔄"; statusText = "Đang xử lý"; break;
                                case "COMPLETED": statusIcon = "✅"; statusText = "Hoàn thành"; break;
                                case "FAILED": statusIcon = "❌"; statusText = "Thất bại"; break;
                            }
                        %>
                        <tr>
                            <td>#<%= task.getId() %></td>
                            <td class="filename-cell"><%= task.getFileName() %></td>
                            <td>
                                <span class="status-badge <%= statusClass %>">
                                    <%= statusIcon %> <%= statusText %>
                                </span>
                            </td>
                            <td><%= sdf.format(task.getSubmissionTime()) %></td>
                            <td>
                                <%= task.getCompletionTime() != null ? sdf.format(task.getCompletionTime()) : "-" %>
                            </td>
                            <td>
                                <%= task.getProcessingTimeMs() != null ? (task.getProcessingTimeMs() / 1000.0) + "s" : "-" %>
                            </td>
                            <td>
                                <div class="action-buttons">
                                    <button class="btn-view" onclick="viewTask(<%= task.getId() %>)" data-tooltip="Xem chi tiết">👁️</button>
                                    <% if ("COMPLETED".equals(task.getStatus())) { %>
                                        <div class="dropdown">
                                            <button class="btn-download" data-tooltip="Tải về">📥</button>
                                            <div class="dropdown-content">
                                                <a href="javascript:void(0)" onclick="downloadFile(<%= task.getId() %>, 'txt')">📄 File TXT</a>
                                                <a href="javascript:void(0)" onclick="downloadFile(<%= task.getId() %>, 'docx')">📝 File Word</a>
                                                <a href="javascript:void(0)" onclick="downloadFile(<%= task.getId() %>, 'pdf')">📕 File PDF</a>
                                            </div>
                                        </div>
                                    <% } %>
                                    <button class="btn-delete" onclick="deleteTask(<%= task.getId() %>)" id="deleteBtn<%= task.getId() %>" data-tooltip="Xóa task">🗑️</button>
                                </div>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } else { %>
                <div class="empty-state">
                    <div class="empty-state-icon">📭</div>
                    <h3>Chưa có task nào</h3>
                    <p>Hãy upload file media để bắt đầu!</p>
                    <br>
                    <a href="<%= request.getContextPath() %>/upload" class="btn btn-primary">📤 Upload File</a>
                </div>
            <% } %>
        </div>
    </div>
    
    <!-- Modal xem chi tiết -->
    <div id="taskModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Chi Tiết Task</h2>
                <span class="close" onclick="closeModal()">&times;</span>
            </div>
            <div id="modalBody">
                <p style="text-align: center; padding: 40px;">Đang tải...</p>
            </div>
        </div>
    </div>
    
    <script>
        let isModalOpen = false;
        let autoRefreshTimer = null;
        
        // Escape HTML để tránh XSS và hiển thị đúng text
        function escapeHtml(text) {
            if (!text) return '';
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
        
        function viewTask(taskId) {
            const modal = document.getElementById('taskModal');
            const modalBody = document.getElementById('modalBody');
            
            isModalOpen = true;
            modal.style.display = 'block';
            modalBody.innerHTML = '<p style="text-align: center; padding: 40px;">⏳ Đang tải...</p>';
            
            fetch('<%= request.getContextPath() %>/api/task/' + taskId + '?format=json')
                .then(response => response.json())
                .then(task => {
                    const statusIcons = {
                        'PENDING': '⏳',
                        'PROCESSING': '🔄',
                        'COMPLETED': '✅',
                        'FAILED': '❌'
                    };
                    
                    const statusTexts = {
                        'PENDING': 'Đang chờ',
                        'PROCESSING': 'Đang xử lý',
                        'COMPLETED': 'Hoàn thành',
                        'FAILED': 'Thất bại'
                    };
                    
                    let html = '';
                    html += '<div class="detail-row">';
                    html += '<label>Task ID:</label>';
                    html += '<div class="value">#' + task.id + '</div>';
                    html += '</div>';
                    
                    html += '<div class="detail-row">';
                    html += '<label>Tên File:</label>';
                    html += '<div class="value">' + escapeHtml(task.fileName) + '</div>';
                    html += '</div>';
                    
                    html += '<div class="detail-row">';
                    html += '<label>Trạng Thái:</label>';
                    html += '<div class="value">';
                    html += '<span class="status-badge status-' + task.status.toLowerCase() + '">';
                    html += statusIcons[task.status] + ' ' + statusTexts[task.status];
                    html += '</span>';
                    html += '</div>';
                    html += '</div>';
                    
                    html += '<div class="detail-row">';
                    html += '<label>Thời Gian Gửi:</label>';
                    html += '<div class="value">' + task.submissionTime + '</div>';
                    html += '</div>';
                    
                    if (task.completionTime) {
                        html += '<div class="detail-row">';
                        html += '<label>Thời Gian Hoàn Thành:</label>';
                        html += '<div class="value">' + task.completionTime + '</div>';
                        html += '</div>';
                    }
                    
                    if (task.processingTimeMs) {
                        let timeInSeconds = (task.processingTimeMs / 1000).toFixed(2);
                        html += '<div class="detail-row">';
                        html += '<label>Thời Gian Xử Lý:</label>';
                        html += '<div class="value">' + timeInSeconds + ' giây</div>';
                        html += '</div>';
                    }
                    
                    if (task.status === 'COMPLETED' && task.resultText) {
                        html += '<div class="detail-row">';
                        html += '<label>Kết Quả Speech-to-Text:</label>';
                        html += '<div class="result-text">' + escapeHtml(task.resultText) + '</div>';
                        html += '</div>';
                        
                        // Thêm nút tải xuống
                        html += '<div class="download-group">';
                        html += '<h3>📥 Tải Xuống Kết Quả</h3>';
                        html += '<button class="btn-download" onclick="downloadFile(' + task.id + ', \'txt\')">📄 Tải xuống TXT</button>';
                        html += '<button class="btn-download" onclick="downloadFile(' + task.id + ', \'docx\')">📝 Tải xuống Word</button>';
                        html += '<button class="btn-download" onclick="downloadFile(' + task.id + ', \'pdf\')">📕 Tải xuống PDF</button>';
                        html += '</div>';
                    }
                    
                    if (task.status === 'FAILED' && task.resultText) {
                        html += '<div class="detail-row">';
                        html += '<label>Lỗi:</label>';
                        html += '<div class="value" style="color: #dc3545;">' + escapeHtml(task.resultText) + '</div>';
                        html += '</div>';
                    }
                    
                    modalBody.innerHTML = html;
                })
                .catch(error => {
                    modalBody.innerHTML = '<p style="text-align: center; padding: 40px; color: red;">❌ Lỗi tải dữ liệu</p>';
                });
        }
        
        function closeModal() {
            document.getElementById('taskModal').style.display = 'none';
            isModalOpen = false;
            // Restart auto-refresh timer when modal is closed
            startAutoRefresh();
        }
        
        function downloadFile(taskId, format) {
            // Tạo URL tải xuống
            const downloadUrl = '<%= request.getContextPath() %>/download/' + taskId + '/' + format;
            
            // Mở URL trong tab mới hoặc tự động tải xuống
            window.location.href = downloadUrl;
            
            // Hiển thị thông báo
            const formatNames = {
                'txt': 'TXT',
                'docx': 'Word (DOCX)',
                'pdf': 'PDF'
            };
            
            console.log('Đang tải xuống task #' + taskId + ' định dạng ' + formatNames[format]);
        }
        
        function deleteTask(taskId) {
            if (!confirm('Bạn có chắc chắn muốn xóa task #' + taskId + ' không?')) {
                return;
            }
            
            const deleteBtn = document.getElementById('deleteBtn' + taskId);
            if (deleteBtn) {
                deleteBtn.disabled = true;
                deleteBtn.textContent = '⏳ Đang xóa...';
            }
            
            fetch('<%= request.getContextPath() %>/history?action=delete&taskId=' + taskId, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert('✅ ' + data.message);
                    // Reload trang để cập nhật danh sách
                    location.reload();
                } else {
                    alert('❌ ' + data.message);
                    if (deleteBtn) {
                        deleteBtn.disabled = false;
                        deleteBtn.textContent = '🗑️ Xóa';
                    }
                }
            })
            .catch(error => {
                alert('❌ Lỗi khi xóa task: ' + error);
                if (deleteBtn) {
                    deleteBtn.disabled = false;
                    deleteBtn.textContent = '🗑️ Xóa';
                }
            });
        }
        
        window.onclick = function(event) {
            const modal = document.getElementById('taskModal');
            if (event.target == modal) {
                modal.style.display = 'none';
                isModalOpen = false;
                // Restart auto-refresh timer when modal is closed
                startAutoRefresh();
            }
        }
        
        // Auto refresh function - only reload if modal is NOT open
        function startAutoRefresh() {
            <% if (processingCount != null && processingCount > 0 || pendingCount != null && pendingCount > 0) { %>
                // Clear existing timer
                if (autoRefreshTimer) {
                    clearTimeout(autoRefreshTimer);
                }
                
                // Set new timer - only refresh if modal is closed
                autoRefreshTimer = setTimeout(function() {
                    if (!isModalOpen) {
                        location.reload();
                    } else {
                        // If modal is open, check again after 5 seconds
                        startAutoRefresh();
                    }
                }, 10000); // Increased to 10 seconds
            <% } %>
        }
        
        // Start auto-refresh when page loads
        startAutoRefresh();
    </script>
</body>
</html>
