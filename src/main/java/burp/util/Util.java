/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp.util;

import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

/**
 *
 * @author Joaquin R. Martinez
 */
public class Util {
	/**
	 * Generates randomStr random string (for Multipart requests)
	 * 
	 * @param lenght the char number of the random string
	 * @return the random string
	 */
	public static String generateRandomString(int lenght) {
		SplittableRandom splittableRandom = new SplittableRandom();
		StringBuffer randomStr = new StringBuffer();
		for (int i = 0; i < lenght; i++) {
			int randInt = splittableRandom.nextInt(0, 2);
			int temp = randInt == 1 ? splittableRandom.nextInt('A', 'Z') : splittableRandom.nextInt('a', 'z');
			randomStr.append((char) temp);
		}
		return randomStr.toString();
	}

}
