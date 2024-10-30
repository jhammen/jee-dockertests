package ee.jakarta.examples.dockertests;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * Hello world!
 *
 */
public class App {
	static class Example {
		public String name;
		public String file;
		public String path;
	}
	public static void main(String[] args) throws Exception {
		// freemarker config
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);
		cfg.setClassForTemplateLoading(App.class, "/");
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);
		cfg.setFallbackOnNullLoopVariable(false);
		cfg.setSQLDateAndTimeTimeZone(TimeZone.getDefault());

		// app servers
		String[] appservers = { "glassfish7", "openliberty23" };
		
		// examples list
		ClassLoader classLoader = App.class.getClassLoader();
		InputStream liststream = classLoader.getResourceAsStream("examples.json");
		ObjectMapper mapper = new ObjectMapper();
		Example[] examples = mapper.readValue(liststream, Example[].class);

		// TODO: loop here
		Example example = examples[0];

		// set up model for template
		Map<String, String> context = new HashMap<String, String>();
		context.put("warfile", example.file);

		// copy configs if needed
		String outpath = "images/" + example.name;
		try {
			Files.copy(Paths.get("config/ol23-server.xml"), Paths.get(outpath + "/ol23-server.xml"));
		} catch (FileAlreadyExistsException ex) {
			System.out.println("* OL server config already exists, delete to recreate");
		}
		// copy war if needed
		String inpath = example.path;
		String warpath = inpath + "/" + example.file;
		try {
			Files.copy(Paths.get(warpath), Paths.get(outpath + "/" + example.file));
		} catch (FileAlreadyExistsException ex) {
			// already there, delete to recreate
			System.out.println("* war file already exists, delete to recreate");
		}

		for (String server : appservers) {
			Template template = cfg.getTemplate(server + ".ftlh");
			String outfilename = example.name + "-" + server + ".dockerfile";
			String outfilepath = outpath + "/" + outfilename;

			FileWriter filewriter = new FileWriter(outfilepath);
			Writer out = new OutputStreamWriter(System.out);
			template.process(context, out);
			template.process(context, filewriter);
			filewriter.close();
		}

	}
}
