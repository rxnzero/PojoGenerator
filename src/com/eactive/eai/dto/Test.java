package com.eactive.eai.dto;

import java.util.ArrayList;
import java.util.List;

import com.eactive.eai.dto.Person.Address;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}
	
	public void run() {
		Person p = new Person();
    	
    	p.setName("Lee");
    	
    	List<Address> addrs = new ArrayList<Address>();
    	
    	Address a = new Address();
    	
    	a.setAddr("address");
    	a.setZipcode("1-1");
    	
    	addrs.add(a);
    	
    	p.setAddress(addrs);
    	
    	System.out.println(p);

	}
	public static void main(String[] args) {
		Test t = new Test();
		t.run();

	}

}
