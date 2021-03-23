package Santander.test;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import Santander.FunctionsSantander;

public class FunctionsSantanderTest {

	FunctionsSantander c1 = new FunctionsSantander();
	
	void main() {
		//c1.getWSLogin("0011227412k", "1357");
	}
	
	@Before
	public void setUp() throws Exception {
		System.out.println("Setting Up...");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetWSLogin() {
		//fail("Not yet implemented");
		
		try {
			c1.getWSLogin("0011227412k", "1357");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
