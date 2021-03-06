package aQute.bnd.main;

import java.io.*;
import java.util.*;

import aQute.bnd.header.*;
import aQute.bnd.main.bnd.ProfileOptions;
import aQute.bnd.osgi.*;
import aQute.bnd.osgi.resource.*;
import aQute.bnd.version.*;
import aQute.lib.getopt.*;
import aQute.lib.io.*;
import aQute.libg.glob.*;

public class Profiles extends Processor {

	private bnd bnd;

	// private ProfileOptions options;

	public Profiles(bnd bnd, ProfileOptions options) {
		super(bnd);
		getSettings(bnd);
		this.bnd = bnd;
		// this.options = options;
	}

	public interface CreateOptions extends Options {
		String[] properties();

		String bsn();

		Version version();

		Instructions match();

		String output();

		Glob extension();
	}

	public void _create(CreateOptions options) throws Exception {
		Builder b = new Builder();
		bnd.addClose(b);

		b.setBase(bnd.getBase());

		if (options.properties() != null) {
			for (String propertyFile : options.properties()) {
				File pf = bnd.getFile(propertyFile);
				b.addProperties(pf);
			}
		}

		if (options.bsn() != null)
			b.setProperty(Constants.BUNDLE_SYMBOLICNAME, options.bsn());

		if (options.version() != null)
			b.setProperty(Constants.BUNDLE_VERSION, options.version().toString());

		Instructions match = options.match();

		Parameters packages = new Parameters();
		Parameters capabilities = new Parameters();

		Collection<String> paths = new ArrayList<String>(new Parameters(b.getProperty("-paths")).keySet());
		if (paths.isEmpty())
			paths = options._arguments();

		trace("input %s", paths);

		ResourceBuilder pb = new ResourceBuilder();

		for (String root : paths) {
			File f = bnd.getFile(root);
			if (!f.exists()) {
				error("could not find %s", f);
			} else {

				Glob g = options.extension();
				if (g == null)
					g = new Glob("*.jar");

				Collection<File> files = IO.tree(f, "*.jar");
				trace("will profile %s", files);

				for (File file : files) {
					Domain domain = Domain.domain(file);
					if (domain == null) {
						error("Not a bundle because no manifest %s", file);
						continue;
					}

					String bsn = domain.getBundleSymbolicName().getKey();
					if (bsn == null) {
						error("Not a bundle because no manifest %s", file);
						continue;
					}

					if (match != null) {
						Instruction instr = match.finder(bsn);
						if (instr == null || instr.isNegated()) {
							trace("skipped %s because of non matching bsn %s", file, bsn);
							continue;
						}
					}

					Parameters eps = domain.getExportPackage();
					Parameters pcs = domain.getProvideCapability();

					trace("parse %s:\ncaps: %s\npac: %s\n", file, pcs, eps);

					packages.mergeWith(eps, false);
					capabilities.mergeWith(pcs, false);

				}

			}
		}

		b.setProperty(Constants.PROVIDE_CAPABILITY, capabilities.toString());
		b.setProperty(Constants.EXPORT_PACKAGE, packages.toString());
		trace("Found %s packages and %s capabilities", packages.size(), capabilities.size());
		Jar jar = b.build();
		File f = b.getOutputFile(options.output());
		trace("Saving as %s", f);
		jar.write(f);
	}

}
