package tests;

import dict.Dictionary;
import secsie.Secsie;

public class Tests {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String path = "g.f.n";
		String[] moms = path.split("\\.", -1);
		System.out.println(moms[0]);
		System.out.println(moms[1]);
		System.out.println(moms[2]);
//		System.out.println(moms[3]);  this is an error
		System.out.println(moms.length);
		
		Dictionary d = new Dictionary();
		System.out.println(d.getClass().getName());
	}

}
