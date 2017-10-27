package com.eactive.eai.dto;
import java.util.List;
public class Person {

    private String name;

    private int age;

    private boolean male;

    private List<Address> address;

    private boolean state;

    public String getName() {return this.name;}
    public void setName(String value) {this.name = value;}

    public int getAge() {return this.age;}
    public void setAge(int value) {this.age = value;}

    public boolean getMale() {return this.male;}
    public void setMale(boolean value) {this.male = value;}

    public List<Address> getAddress() {return this.address;}
    public void setAddress(List<Address> value) {this.address = value;}

    public boolean getState() {return this.state;}
    public void setState(boolean value) {this.state = value;}

    public class Address {

        private String addr;

        private String zipcode;

        public String getAddr() {return this.addr;}
        public void setAddr(String value) {this.addr = value;}

        public String getZipcode() {return this.zipcode;}
        public void setZipcode(String value) {this.zipcode = value;}

    }

}