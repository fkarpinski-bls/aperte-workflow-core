package org.aperteworkflow.webapi.main;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.web.controller.ControllerMethod;
import pl.net.bluesoft.rnd.processtool.web.controller.IOsgiWebController;
import pl.net.bluesoft.rnd.processtool.web.controller.NoTransactionOsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.controller.OsgiWebRequest;
import pl.net.bluesoft.rnd.processtool.web.domain.GenericResultBean;
import pl.net.bluesoft.rnd.processtool.web.domain.IProcessToolRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Dispatcher for osgi plugins
 *
 * @author: "mpawlak@bluesoft.net.pl"
 */
@Controller
public class DispatcherController extends AbstractProcessToolServletController
{
	private static Logger logger = Logger.getLogger(DispatcherController.class.getName());


	@RequestMapping(value = "/dispatcher/{controllerName}/{actionName}")
	@ResponseBody
	public Object invoke(final @PathVariable String controllerName, @PathVariable String actionName, final HttpServletRequest request, final HttpServletResponse response)
	{
		return invokeExternalController(controllerName, actionName, request, response);
	}

	public Object invokeExternalController(final String controllerName, final String actionName, final HttpServletRequest request, final HttpServletResponse response)
	{
		final GenericResultBean resultBean = new GenericResultBean();
		final IProcessToolRequestContext context = this.initilizeContext(request, getProcessToolRegistry().getProcessToolSessionFactory());

		if(!context.isUserAuthorized())
		{
			resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.nouser"));
			return resultBean;
		}

        /* Find controller in registry */
		final IOsgiWebController servletController = getProcessToolRegistry().getGuiRegistry().getWebController(controllerName);

		if(servletController == null)
		{
			resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.controller.invalidname"));
			return resultBean;
		}

        /* Find controller method by ControllerMethod annotation */
		final OsgiControllerMethod controllerMethod = findAnnotatedMethod(servletController, actionName);

		if(controllerMethod == null)
		{
			resultBean.addError(SYSTEM_SOURCE, context.getMessageSource().getMessage("request.handle.error.controller.nomethodforaction"));
			return resultBean;
		}

		/** Start transaction automatically */
		if(controllerMethod.getControllerMethod().transactionEnabled())
		{
			return getProcessToolRegistry().withProcessToolContext(new ReturningProcessToolContextCallback<Object>() {
				@Override
				public Object processWithContext(ProcessToolContext ctx)
				{
					OsgiWebRequest controllerInvocation = new OsgiWebRequest();
					controllerInvocation.setProcessToolRequestContext(context);
					controllerInvocation.setRequest(request);
					controllerInvocation.setResponse(response);
					controllerInvocation.setProcessToolContext(ctx);

					GenericResultBean result = null;
					try {
						result = (GenericResultBean)controllerMethod.getJavaMethod().invoke(servletController, controllerInvocation);
						return result;

					}
					catch (Throwable e)
					{
						if(result != null)
							resultBean.copyErrors(result);
						else {
							resultBean.addError(SYSTEM_SOURCE, e.getMessage());
						}
						logger.log(Level.SEVERE, "Problem during plugin request processing in dispatcher ["+controllerName+"]", e);
						return resultBean;
					}
				}
			});
		}
		else
		{
			NoTransactionOsgiWebRequest noTransactionOsgiWebRequest = new NoTransactionOsgiWebRequest();
			noTransactionOsgiWebRequest.setProcessToolRequestContext(context);
			noTransactionOsgiWebRequest.setRequest(request);
			noTransactionOsgiWebRequest.setResponse(response);

			GenericResultBean result = null;

			try {
				result = (GenericResultBean)controllerMethod.getJavaMethod().invoke(servletController, noTransactionOsgiWebRequest);
				return result;

			}
			catch (Throwable e)
			{
				if(result != null)
					resultBean.copyErrors(result);
				else {
					resultBean.addError(SYSTEM_SOURCE, e.getMessage());
				}
				logger.log(Level.SEVERE, "Problem during plugin request processing in dispatcher ["+controllerName+"]", e);
				return resultBean;
			}
		}





	}


	/** Find controller method by ControllerMethod annotation */
	private OsgiControllerMethod findAnnotatedMethod(IOsgiWebController servletController, String actionName)
	{

		for(Method method: servletController.getClass().getMethods())
		{
			ControllerMethod controllerMethodAnnotation = method.getAnnotation(ControllerMethod.class);
			if(controllerMethodAnnotation == null)
				continue;

			if(!controllerMethodAnnotation.action().equals(actionName))
				continue;

			OsgiControllerMethod osgiControllerMethod = new OsgiControllerMethod();
			osgiControllerMethod.setControllerMethod(controllerMethodAnnotation);
			osgiControllerMethod.setJavaMethod(method);

			return osgiControllerMethod;
		}

		return null;
	}


	private class OsgiControllerMethod
	{
		private ControllerMethod controllerMethod;
		private Method javaMethod;

		public ControllerMethod getControllerMethod() {
			return controllerMethod;
		}

		public void setControllerMethod(ControllerMethod controllerMethod) {
			this.controllerMethod = controllerMethod;
		}

		public Method getJavaMethod() {
			return javaMethod;
		}

		public void setJavaMethod(Method javaMethod) {
			this.javaMethod = javaMethod;
		}
	}
}
