package secsie;

import dict.Dictionary;
import dict.KeyError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public abstract class Secsie {
	
	// Different section regexes for the different modes
	private static final Pattern secsieSection = Pattern.compile("^\\[([a-zA-Z0-9_-]+)\\]$");
	private static final Pattern iniSection = Pattern.compile("^\\[([a-zA-Z0-9 _-]+)\\]$");
	
	// All other types
	private static final Pattern decimal = Pattern.compile("^([-]?\\d+[\\.]\\d*)$");
	private static final Pattern falsey = Pattern.compile("^(false|no)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern none = Pattern.compile("^null$", Pattern.CASE_INSENSITIVE);
	private static final Pattern truthy = Pattern.compile("^(true|yes)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern integer = Pattern.compile("^[-]?\\d+$");
	
	
	private static Dictionary writeToConf(Dictionary conf, String line, int lineNumber, String section, String mode) throws InvalidSyntax, NumberFormatException, KeyError {
		// Split the line at the equals sign
		String[] keyValue = line.split("=", 2);
		
		// Make sure we have a key and a value
		if (keyValue.length != 2) {
			throw new InvalidSyntax(line, "bad section descriptor or value assignment", lineNumber);
		}
		
		String key = keyValue[0].strip();
		String value = keyValue[1].strip();
		
		if (section != null) {
			key = section + "." + key;
		}
		
		if (key.contains(" ")) {
			throw new InvalidSyntax("spaces not allowed in keys", lineNumber);
		}
		
		// correct ini string if needed by removing quotes from around it
		if (mode == "ini") {
			if (value.startsWith("\"")) {
				value = value.substring(1, value.length()-1);
			}
		}
		
		// Check for special values with regexes
		Matcher decimalMatcher = decimal.matcher(value);
		if (decimalMatcher.find()) {
			conf.set(key, Double.parseDouble(value));
		} else if (falsey.matcher(value).find()) {
			conf.set(key, false);
		} else if (none.matcher(value).find()) {
			conf.set(key, null);
		} else if (truthy.matcher(value).find()) {
			conf.set(key, true);
		} else if (integer.matcher(value).find()) {
			conf.set(key, Integer.parseInt(value));
		} else {
			// Well then it's just gonna be a string
			conf.set(key, value);
		}
		

		return conf;
		
	}
	
	
	
	public static Dictionary parseConfig(String config, String mode) throws NumberFormatException, InvalidSyntax, KeyError {
		String[] lines = config.split("\\r?\\n");
		Dictionary conf = new Dictionary();
		
		String cSection = null;
		for (int lineno = 0; lineno < lines.length; lineno++) {
			String line = lines[lineno].split("#", 2)[0].strip();
			
			// Skip blank lines and comment lines
			if (line == "" || line.startsWith("#") || line.startsWith(";")) {
				continue;
			}
			
			// Check if this is a section declaration
			if (mode == "secsie" || mode == "") {
				Matcher secsie = secsieSection.matcher(line);
				if (secsie.matches()) {
					cSection = secsie.group(1);
					continue;
				}
			} else if (mode == "ini") {
				Matcher ini = iniSection.matcher(line);
				if (ini.matches()) {
					cSection = ini.group(1);
					continue;
				}
			}
			// Save the line in the conf
			conf = writeToConf(conf, line, lineno, cSection, mode);
			
		}
		
		
		return conf;
	}
	
	
	public static Dictionary parseConfigFile(String filepath, String mode) throws IOException, NumberFormatException, InvalidSyntax, KeyError {
		String config = Files.readString(Paths.get(filepath));
		return parseConfig(config, mode);
	}
	
}
