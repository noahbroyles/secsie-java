package tests;


import dict.KeyError;
import dict.Dictionary;
import com.pro.secsie.*;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import com.pro.secsie.InvalidSyntax;

import static org.junit.jupiter.api.Assertions.*;

class SecsieTests {

	@Test
	void testConfigParse() {
		
		String config = """
				; This is an example of a valid secsie file

				before_section = totally okay

				# Whitespace don't matter

				; Here are examples of how types are interpreted(no keywords are off limits!)
				[special_values]
				    int = 42
				    float = 269.887  # my man!
				    truth = yes
				    falsehood = no
				    true = true
				    ; I don't encourage this but it's valid ;)
				    false = FaLSe

				    # Null value
				    nah.ninja = Null

				[anotherSection]
				    # The indent here is optional, included for readability
				    sections = are amazing
				""";
		
		try {
			Dictionary d = Secsie.parseConfig(config, "secsie");
			System.out.println(d);
			assertEquals(d.select("anotherSection.sections"), "are amazing");
		} catch (NumberFormatException | InvalidSyntax | KeyError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	void testFileParse() throws NumberFormatException, IOException, InvalidSyntax, KeyError {
		String path = "/home/nbroyles/PycharmProjects/secsie-conf/examples/valid.secsie.conf";
		
		Dictionary conf = Secsie.parseConfigFile(path, "secsie");
		assertEquals(conf.select("special_values.int"), 42);
		System.out.println(Secsie.generateConfig(conf));
		Secsie.generateConfigFile(conf, "/home/nbroyles/eclipse-workspace/SecsieConf/output.secsie");
			
	}

	@Test
	void testPoundSignInValue() throws NumberFormatException, InvalidSyntax, KeyError {
		String config = """
				# I'm gonna need this line to be ignored
				password = som#taki$ # yes sirree
				jim = the#1\040
				""";

		Dictionary conf = Secsie.parseConfig(config, "secsie");
		assertEquals("som#taki$", conf.get("password"));
		assertEquals("the#1", conf.get("jim"));
	}
}
