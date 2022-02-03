import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import dict.Dictionary;
import dict.KeyError;
import secsie.*;

class SecsieTests {

	@Test
	void testConfigParse() {
		
		String config = "; This is an example of a valid secsie file\n" + 
				"\n" + 
				"before_section = totally okay\n" + 
				"\n" + 
				"# Whitespace don't matter\n" + 
				"\n" + 
				"; Here are examples of how types are interpreted(no keywords are off limits!)\n" + 
				"[special_values]\n" + 
				"    int = 42\n" + 
				"    float = 269.887  # my man!\n" + 
				"    truth = yes\n" + 
				"    falsehood = no\n" + 
				"    true = true\n" + 
				"    ; I don't encourage this but it's valid ;)\n" + 
				"    false = FaLSe\n" + 
				"\n" + 
				"    # Null value\n" + 
				"    nah.ninja = Null\n" + 
				"\n" + 
				"[anotherSection]\n" + 
				"    # The indent here is optional, included for readability\n" + 
				"    sections = are amazing\n" + 
				"";
		
		try {
			Dictionary d = Secsie.parseConfig(config, "secsie");
			System.out.println(d);
			assertEquals(d.get("anotherSection.sections"), "are amazing");
		} catch (NumberFormatException | InvalidSyntax | KeyError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	void testFileParse() throws NumberFormatException, IOException, InvalidSyntax, KeyError {
		String path = "/home/nbroyles/PycharmProjects/secsie-conf/examples/valid.secsie.conf";
		
			Dictionary conf = Secsie.parseConfigFile(path, "secsie");
			assertEquals(conf.get("special_values.int"), 42);
			
		}
}
