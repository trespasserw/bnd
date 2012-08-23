package aQute.bnd.ant;

import org.apache.tools.ant.*;

import aQute.bnd.build.*;
import aQute.bnd.build.Project;

/**
 * <p>
 * ANT task to release into a repository, equivalent to:
 * <code>&lt;bnd command="release"/&gt;</code>
 * </p>
 * <p>
 * To release into the <em>default</em> repository (defined by
 * <code>-releaserepo</code> in <code>build.bnd</code>):
 * </p>
 * 
 * <pre>
 *    &lt;bndrelease/&gt;
 * </pre>
 * <p>
 * To release into a specific named repository:
 * 
 * <pre>
 *    &lt;bndrelease releaserepo="My Repository"/&gt;
 * </pre>
 * 
 * @author Neil Bartlett
 * @see {@link BndTask} for setup instructions.
 */
public class ReleaseTask extends BaseTask {

	String	releaseRepo	= null;

	@Override
	public void execute() throws BuildException {
		try {
			Project project = Workspace.getProject(getProject().getBaseDir());
			project.release(releaseRepo);

			if (report(project))
				throw new BuildException("Release failed");
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(e);
		}
	}

	public void setReleaserepo(String releaseRepo) {
		this.releaseRepo = releaseRepo;
	}

}
