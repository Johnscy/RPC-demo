import java.io.IOException;
import java.net.InetSocketAddress;

public class RPCTest {
    public static void main(String[] args) throws IOException {
        MyHelloService service = Client.getRemoteProxyObj(MyHelloService.class,new InetSocketAddress(8080));
        System.out.println(service.sayHi("zhebang","hahaha"));
    }
}
