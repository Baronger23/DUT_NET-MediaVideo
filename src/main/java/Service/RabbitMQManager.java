//package Service;
//
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
//import com.rabbitmq.client.DeliverCallback;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.concurrent.TimeoutException;
//
///**
// * RabbitMQ Manager - Message Broker bên ngoài
// * Thay thế BlockingQueue bằng RabbitMQ để đạt điểm cao
// * 
// * ✅ Ưu điểm:
// * - Message broker chuyên nghiệp
// * - Hỗ trợ phân tán (distributed)
// * - Persistent (lưu trữ message khi restart)
// * - Load balancing tự động
// * - Thể hiện kiến trúc microservices
// */
//public class RabbitMQManager {
//    private static RabbitMQManager instance;
//    
//    // Cấu hình RabbitMQ
//    private static final String RABBITMQ_HOST = "localhost";
//    private static final int RABBITMQ_PORT = 5672;
//    private static final String RABBITMQ_USERNAME = "guest";
//    private static final String RABBITMQ_PASSWORD = "guest";
//    
//    // Queue name
//    private static final String TASK_QUEUE_NAME = "media_processing_queue";
//    
//    private ConnectionFactory factory;
//    private Connection connection;
//    private Channel channel;
//    
//    private RabbitMQManager() {
//        try {
//            initializeRabbitMQ();
//        } catch (Exception e) {
//            System.err.println("❌ Lỗi khởi tạo RabbitMQ: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//    
//    /**
//     * Khởi tạo kết nối RabbitMQ
//     */
//    private void initializeRabbitMQ() throws IOException, TimeoutException {
//        factory = new ConnectionFactory();
//        factory.setHost(RABBITMQ_HOST);
//        factory.setPort(RABBITMQ_PORT);
//        factory.setUsername(RABBITMQ_USERNAME);
//        factory.setPassword(RABBITMQ_PASSWORD);
//        
//        // Tạo connection
//        connection = factory.newConnection();
//        channel = connection.createChannel();
//        
//        // Khai báo queue (durable = true để persistent)
//        // Queue sẽ tồn tại ngay cả khi RabbitMQ restart
//        channel.queueDeclare(
//            TASK_QUEUE_NAME,  // Queue name
//            true,             // Durable (persistent)
//            false,            // Exclusive
//            false,            // Auto-delete
//            null              // Arguments
//        );
//        
//        // Cấu hình prefetch để worker chỉ nhận 1 task tại 1 thời điểm
//        channel.basicQos(1);
//        
//        System.out.println("✅ RabbitMQ đã được khởi tạo thành công");
//        System.out.println("   - Host: " + RABBITMQ_HOST + ":" + RABBITMQ_PORT);
//        System.out.println("   - Queue: " + TASK_QUEUE_NAME);
//    }
//    
//    /**
//     * Singleton instance
//     */
//    public static synchronized RabbitMQManager getInstance() {
//        if (instance == null) {
//            instance = new RabbitMQManager();
//        }
//        return instance;
//    }
//    
//    /**
//     * Đẩy Task ID vào RabbitMQ Queue
//     * @param taskId ID của task cần xử lý
//     * @return true nếu thành công
//     */
//    public boolean enqueue(int taskId) {
//        try {
//            String message = String.valueOf(taskId);
//            
//            // Publish message với persistent flag
//            channel.basicPublish(
//                "",                     // Exchange (default)
//                TASK_QUEUE_NAME,        // Routing key (queue name)
//                null,                   // Basic properties
//                message.getBytes(StandardCharsets.UTF_8)
//            );
//            
//            System.out.println("[RabbitMQ] ✓ Đã đẩy Task ID: " + taskId + " vào queue");
//            return true;
//            
//        } catch (Exception e) {
//            System.err.println("[RabbitMQ] ❌ Lỗi khi đẩy task vào queue: " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        }
//    }
//    
//    /**
//     * Worker đăng ký nhận message từ RabbitMQ
//     * @param callback Hàm callback xử lý khi nhận được task
//     */
//    public void consumeMessages(MessageCallback callback) {
//        try {
//            System.out.println("[RabbitMQ] Worker đang lắng nghe queue: " + TASK_QUEUE_NAME);
//            
//            // Callback khi nhận được message
//            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//                
//                try {
//                    int taskId = Integer.parseInt(message);
//                    System.out.println("[RabbitMQ] ◄ Nhận Task ID: " + taskId);
//                    
//                    // Gọi callback để xử lý task
//                    callback.onMessageReceived(taskId);
//                    
//                    // Acknowledge message sau khi xử lý xong
//                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                    System.out.println("[RabbitMQ] ✓ Đã ACK Task ID: " + taskId);
//                    
//                } catch (Exception e) {
//                    System.err.println("[RabbitMQ] ❌ Lỗi xử lý message: " + e.getMessage());
//                    e.printStackTrace();
//                    
//                    // Reject message (có thể requeue hoặc bỏ qua)
//                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
//                }
//            };
//            
//            // Bắt đầu consume (autoAck = false để manual acknowledge)
//            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {});
//            
//        } catch (Exception e) {
//            System.err.println("[RabbitMQ] ❌ Lỗi khi consume messages: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//    
//    /**
//     * Lấy số lượng message đang chờ trong queue
//     */
//    public long getQueueSize() {
//        try {
//            return channel.messageCount(TASK_QUEUE_NAME);
//        } catch (Exception e) {
//            System.err.println("[RabbitMQ] Lỗi khi lấy queue size: " + e.getMessage());
//            return 0;
//        }
//    }
//    
//    /**
//     * Đóng kết nối RabbitMQ
//     */
//    public void close() {
//        try {
//            if (channel != null && channel.isOpen()) {
//                channel.close();
//            }
//            if (connection != null && connection.isOpen()) {
//                connection.close();
//            }
//            System.out.println("[RabbitMQ] Đã đóng kết nối");
//        } catch (Exception e) {
//            System.err.println("[RabbitMQ] Lỗi khi đóng kết nối: " + e.getMessage());
//        }
//    }
//    
//    /**
//     * Interface cho callback khi nhận message
//     */
//    public interface MessageCallback {
//        void onMessageReceived(int taskId);
//    }
//}
