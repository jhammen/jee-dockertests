package ee.jakarta.examples.dockertests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Hello world!
 *
 */
public class App {

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
		Server[] appservers = { new Server("glassfish7", "8080"), new Server("openliberty23", "9080"),
				new Server("wildfly33", "8080") };

		// examples list
		ClassLoader classLoader = App.class.getClassLoader();
		InputStream liststream = classLoader.getResourceAsStream("examples.json");
		ObjectMapper mapper = new ObjectMapper();
		Example[] examples = mapper.readValue(liststream, Example[].class);

		// loop over examples
		for (Example example : examples) {

			// set up model for template
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("example", example);

			// create folder if needed
			String outpath = "images/" + example.getName();
			File outfile = new File(outpath);
			if (!outfile.exists()) {
				outfile.mkdirs();
			}

			// copy war if needed
			String inpath = example.getPath();
			String warpath = inpath + "/" + example.getFile();
			try {
				Files.copy(Paths.get(warpath), Paths.get(outpath + "/" + example.getFile()));
			} catch (FileAlreadyExistsException ex) {
				// already there, delete to recreate
				System.out.println("* war file already exists, delete to recreate");
			}

			// write open liberty server xml
			Template otemplate = cfg.getTemplate("ol23-server.ftlh");
			writeTemplate(context, otemplate, outpath + "/ol23-server.xml");


			for (Server server : appservers) {
				Template template = cfg.getTemplate(server.getName() + ".ftlh");
				String outfilename = example.getName() + "-" + server.getName() + ".dockerfile";
				String outfilepath = outpath + "/" + outfilename;

				FileWriter filewriter = new FileWriter(outfilepath);
				Writer out = new OutputStreamWriter(System.out);
				template.process(context, out);
				template.process(context, filewriter);
				filewriter.close();

				// create build + run script files
				context.put("server", server);
				Template btemplate = cfg.getTemplate("build.ftlh");
				String buildpath = outpath + "/build-" + example.getName() + "-" + server.getName() + ".sh";
				writeTemplate(context, btemplate, buildpath);
				new File(buildpath).setExecutable(true);

				Template rtemplate = cfg.getTemplate("run.ftlh");
				String runpath = outpath + "/run-" + example.getName() + "-" + server.getName() + ".sh";
				writeTemplate(context, rtemplate, runpath);
				new File(runpath).setExecutable(true);
				
				Template itemplate = cfg.getTemplate("index.ftlh");
				String htmlpath = outpath + "/test-" + server.getName() + ".html";
				writeTemplate(context, itemplate, htmlpath);
			}
		}
	}

	private static void writeTemplate(Map<String, Object> context, Template btemplate, String buildpath)
			throws IOException, TemplateException {
		FileWriter bfilewriter = new FileWriter(buildpath);
		btemplate.process(context, bfilewriter);
		bfilewriter.close();
	}
}
