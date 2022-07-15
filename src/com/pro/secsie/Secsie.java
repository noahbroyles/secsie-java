package com.pro.secsie;


import dict.KeyError;
import dict.Dictionary;
import java.util.Objects;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public abstract class Secsie {
	
	// Different section regexes for the different modes
	private static final Pattern iniSection = Pattern.compile("^\\[([a-zA-Z0-9 _-]+)\\]$");
	private static final Pattern secsieSection = Pattern.compile("^\\[([a-zA-Z0-9_-]+)\\]$");

	// All other types
	private static final Pattern integer = Pattern.compile("^-?\\d+$");
	private static final Pattern decimal = Pattern.compile("^(-?\\d+[\\.]\\d*)$");
	private static final Pattern none = Pattern.compile("^null$", Pattern.CASE_INSENSITIVE);
	private static final Pattern falsey = Pattern.compile("^(false|no)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern truthy = Pattern.compile("^(true|yes)$", Pattern.CASE_INSENSITIVE);

	
	private static Dictionary writeToConf(Dictionary conf, String line, int lineNumber, String section, String mode) throws InvalidSyntax, NumberFormatException {
		// Split the line at the equals sign
		String[] keyValue = line.split("=", 2);
		
		// Make sure we have a selector and a value
		if (keyValue.length != 2) {
			throw new InvalidSyntax(line, "bad section descriptor or value assignment", lineNumber);
		}

		String value = keyValue[1].strip();
		String selector = keyValue[0].strip();
		String sepRegex = "\\>";  // Used as the separator for the Dictionary selector

		// Do Mode Specific things
		if (mode.equals("ini")) {
			// correct INI string if needed by removing quotes from around it
			if (value.startsWith("\"")) {
				value = value.substring(1, value.length()-1);
			}
		}

		if (section != null) {
			// If we are in a section, adjust the dictionary key/selector
			selector = section + ">" + selector;
		}
		
		if (selector.contains(" ")) {
			throw new InvalidSyntax("spaces not allowed in keys", lineNumber);
		}
		
		// Check for special values with regexes
		try {
			if (decimal.matcher(value).find()) {
				conf.set(selector, Double.parseDouble(value), sepRegex);
			} else if (falsey.matcher(value).find()) {
				conf.set(selector, false, sepRegex);
			} else if (none.matcher(value).find()) {
				conf.set(selector, null, sepRegex);
			} else if (truthy.matcher(value).find()) {
				conf.set(selector, true, sepRegex);
			} else if (integer.matcher(value).find()) {
				conf.set(selector, Integer.parseInt(value), sepRegex);
			} else {
				// Well then it's just gonna be a string
				conf.set(selector, value, sepRegex);
			}
		} catch (KeyError ignored) {}  // Ignored because it will never happen

		return conf;
		
	}
	
	
	
	public static Dictionary parseConfig(String config, String mode) throws NumberFormatException, InvalidSyntax, KeyError {
		String[] lines = config.split("\\r?\\n");
		Dictionary conf = new Dictionary();
		
		String cSection = null;
		for (int lineno = 0; lineno < lines.length; lineno++) {
			String line = lines[lineno].split(" #", 2)[0].strip();
			
			// Skip blank lines and comment lines
			if (Objects.equals(line, "") || line.startsWith("#") || line.startsWith(";")) {
				continue;
			}
			
			// Check if this is a section declaration
			if (mode.equals("secsie") || mode.equals("")) {
				Matcher secsie = secsieSection.matcher(line);
				if (secsie.matches()) {
					cSection = secsie.group(1);
					continue;
				}
			} else if (mode.equals("ini")) {
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
	
	
	public static String generateConfig(Dictionary config) {
		StringBuilder conf = new StringBuilder();
		for (String key: config.keySet()) {
			Object value = config.get(key);
			if (value.getClass() == Dictionary.class) {
				conf.append("\n[").append(key.replaceAll("\\s", "")).append("]\n");
				for (String k: ((Dictionary) value).keySet()) {
					if (((Dictionary) value).get(k) == "") {
						conf.append(";");
					}
					conf.append("\t").append(k).append(" = ").append(((Dictionary) value).get(k)).append("\n");
				}
				conf.append("\n");
				continue;
			}
			conf.append(key).append(" = ").append(value).append("\n");
			
		}
		
		return conf.toString();
	}
	
	
	public static void generateConfigFile(Dictionary config, String outputPath) throws KeyError, IOException {
		String conf = generateConfig(config);
		
		FileWriter dubby = new FileWriter(outputPath);
		BufferedWriter writer = new BufferedWriter(dubby);
		writer.write("; auto-generated by secsie\n" + conf);
		writer.close();
	}
	
	
}
