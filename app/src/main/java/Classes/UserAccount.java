package Classes;


public class UserAccount {

    private String email;
    private String password;
    private String name;
    private int age;
    private String location;

    public UserAccount(String email, String password, String name, int age, String location){
        this.email = email;
        this.password = password;
        this.name = name;
        this.age = age;
        this.location = location;
    }

    public String getEmail(){
        return this.email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getAge(){
        return this.age;
    }

    public void setAge(int age){
        this.age = age;
    }

    public String getLocation(){
        return this.location;
    }

    public void setLocation(String location){
        this.location = location;
    }
}
