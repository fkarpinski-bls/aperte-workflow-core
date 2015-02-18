package pl.net.bluesoft.rnd.processtool.plugins;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.UserData;

/**
 * User: POlszewski
 * Date: 2015-02-18
 */
public interface ActionPermissionChecker {
	Boolean hasPermission(String actionName, BpmTask task, UserData user);
}
