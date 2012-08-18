package be.betty.gwtp.client.action;

import com.gwtplatform.dispatch.shared.ActionImpl;

public class GetProjectsAction extends ActionImpl<GetProjectsActionResult> {

	private String session_id;

	@SuppressWarnings("unused")
	private GetProjectsAction() {
		// For serialization only
	}

	@Override
	public boolean isSecured() {
		return false;
	}

	public GetProjectsAction(String sess) {
		this.session_id = sess;
	}

	public String getSession_id() {
		return session_id;
	}
}
