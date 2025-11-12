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
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        
        .header {
            background: white;
            padding: 20px;
            border-radius: 10px 10px 0 0;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .header h1 {
            color: #667eea;
            font-size: 24px;
        }
        
        .user-info {
            display: flex;
            align-items: center;
            gap: 15px;
        }
        
        .nav-links {
            display: flex;
            gap: 10px;
        }
        
        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 14px;
            text-decoration: none;
            display: inline-block;
            transition: all 0.3s;
        }
        
        .btn-primary {
            background: #667eea;
            color: white;
        }
        
        .btn-primary:hover {
            background: #5568d3;
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #5a6268;
        }
        
        .main-content {
            background: white;
            padding: 30px;
            border-radius: 0 0 10px 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            padding: 20px;
            border-radius: 10px;
            text-align: center;
            color: white;
        }
        
        .stat-card.total {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        
        .stat-card.pending {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }
        
        .stat-card.processing {
            background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        }
        
        .stat-card.completed {
            background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        }
        
        .stat-card.failed {
            background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
        }
        
        .stat-card h3 {
            font-size: 36px;
            margin-bottom: 10px;
        }
        
        .stat-card p {
            font-size: 14px;
            opacity: 0.9;
        }
        
        .task-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            table-layout: fixed;
        }
        
        .task-table th {
            background: #f8f9fa;
            padding: 15px;
            text-align: left;
            font-weight: 600;
            color: #333;
            border-bottom: 2px solid #dee2e6;
        }
        
        .task-table td {
            padding: 15px;
            border-bottom: 1px solid #dee2e6;
            word-wrap: break-word;
            overflow-wrap: break-word;
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
            padding: 5px 15px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
            display: inline-block;
        }
        
        .status-pending {
            background: #fff3cd;
            color: #856404;
        }
        
        .status-processing {
            background: #d1ecf1;
            color: #0c5460;
        }
        
        .status-completed {
            background: #d4edda;
            color: #155724;
        }
        
        .status-failed {
            background: #f8d7da;
            color: #721c24;
        }
        
        .btn-view {
            padding: 5px 15px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 12px;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn-view:hover {
            background: #5568d3;
        }
        
        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #666;
        }
        
        .empty-state-icon {
            font-size: 64px;
            margin-bottom: 20px;
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
            padding: 30px;
            border-radius: 10px;
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
            padding-bottom: 15px;
            border-bottom: 2px solid #dee2e6;
        }
        
        .close {
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
            color: #666;
        }
        
        .close:hover {
            color: #000;
        }
        
        .detail-row {
            padding: 15px 0;
            border-bottom: 1px solid #dee2e6;
        }
        
        .detail-row label {
            font-weight: 600;
            color: #333;
            display: block;
            margin-bottom: 5px;
        }
        
        .detail-row .value {
            color: #666;
        }
        
        .result-text {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 5px;
            white-space: pre-wrap;
            word-wrap: break-word;
            max-height: 500px;
            overflow-y: auto;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            font-size: 15px;
            line-height: 1.8;
            border: 1px solid #dee2e6;
            color: #333;
        }
        
        .refresh-btn {
            float: right;
            margin-bottom: 20px;
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
                                <button class="btn-view" onclick="viewTask(<%= task.getId() %>)">👁️ Xem</button>
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
