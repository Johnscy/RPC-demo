import java.io.IOException;

public interface Server {
    //socket端口
    int PORT = 8080;

    //启动服务
    void start() throws IOException;

    //终止服务
    void stop();

    //注册服务
    void registry(Class<? extends MyRPCService> serviceInterface, Class<? extends  MyRPCService> serviceImpl);
}
