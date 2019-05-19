import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {
    @SuppressWarnings("unchecked")
    public static <T extends MyRPCService> T getRemoteProxyObj(final Class<? extends MyRPCService> serviceInterface, final InetSocketAddress addr){
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class[]{serviceInterface}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Socket socket = null;
                ObjectOutputStream output = null;
                ObjectInputStream input = null;
                try{
                    //创建socket客户端，根据指定地址连接远程服务提供者
                    socket = new Socket();
                    socket.connect(addr);

                    //将远程服务调用所需的接口类、方法名、参数列表等编码后发送给服务提供者
                    output = new ObjectOutputStream(socket.getOutputStream());
                    output.writeUTF(serviceInterface.getName());
                    output.writeUTF(method.getName());
                    output.writeObject(method.getParameterTypes());
                    output.writeObject(args);

                    //同步阻塞等待服务器端返回应答
                    input = new ObjectInputStream(socket.getInputStream());
                    return input.readObject();
                }finally {
                    if (socket != null){
                        socket.close();
                    }
                    if (output != null){
                        output.close();
                    }
                    if (input != null){
                        input.close();
                    }
                }
            }
        });
    }
}
