import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerCenter implements Server {
    //线程池处理客户端请求
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 20, 200, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(10));

    //服务注册缓存，用HashMap装载
    private static final Map<String,Class<?>> serviceRegistry = new HashMap<>();

    //实现启动服务
    @Override
    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(); //服务端Socket
        serverSocket.bind(new InetSocketAddress(PORT));
        try {
            while (true){
                executor.execute(new ServiceTask(serverSocket.accept()));   //serverSocket.accept()在有连接请求时才会返回，并且返回值是一个Socket类的实例
            }
        }finally {
            serverSocket.close();
        }

    }

    //实现终止服务
    @Override
    public void stop(){
        executor.shutdown();
    }

    //服务注册缓存
    @Override
    public void registry(Class<? extends MyRPCService> serviceInterface, Class<? extends  MyRPCService> serviceImpl){
        serviceRegistry.put(serviceInterface.getName(),serviceImpl);
    }

    //服务任务，实现Runnable接口定义任务
    private static class ServiceTask implements Runnable{
        Socket client = null;

        public ServiceTask(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;
            try {
                input = new ObjectInputStream(client.getInputStream());
                String serviceName = input.readUTF();
                String methodName = input.readUTF();

                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                Object[] arguments = (Object[]) input.readObject();
                Class<?> serviceClass = serviceRegistry.get(serviceName);
                if (serviceClass == null) {
                    throw new ClassNotFoundException(serviceName + "not found");
                }
                Method method = serviceClass.getMethod(methodName,parameterTypes);
                Object result = method.invoke(serviceClass.newInstance(),arguments);

                    //将结果反序列化，通过socket返回给客户端
                output = new ObjectOutputStream(client.getOutputStream());
                output.writeObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (input != null){
                    try{
                        input.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

                if (output != null){
                    try{
                        output.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }

                if (client != null){
                    try{
                        client.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public static void main(String[] args) throws Exception{
        ServerCenter serverCenter = new ServerCenter();
        serverCenter.registry(MyHelloService.class,new MyHelloServiceImpl().getClass());
        serverCenter.start();
    }
}
