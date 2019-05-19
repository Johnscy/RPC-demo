public class MyHelloServiceImpl implements MyHelloService {
    private  static final long serialVersionUID = 146468468464364698L;
    @Override
    public String sayHi(String name, String message){
        return new StringBuilder().append("Hi,").append(name).append(".").append(message).toString();
    }
}
