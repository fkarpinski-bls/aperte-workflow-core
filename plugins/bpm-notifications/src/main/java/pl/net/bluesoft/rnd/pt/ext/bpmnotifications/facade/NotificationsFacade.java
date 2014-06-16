package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.facade;

import java.util.*;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotification;
import pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model.BpmNotificationMailProperties;

/**
 * Facade layer for the Notification database access
 * 
 * @author Maciej Pawlak
 *
 */
public class NotificationsFacade 
{
	/** Get all notifications waiting to be sent */
	public static Collection<BpmNotification> getNotificationsToSend()
	{			
		return (List<BpmNotification>)getSession()
				.createCriteria(BpmNotification.class)
				.add(Restrictions.eq("groupNotifications", false))
				.setLockMode(LockMode.UPGRADE_NOWAIT)
				.addOrder(Order.asc("recipient"))
				.list();
	}
	
	/** Get all notifications waiting to be sent for grouping */
	public static Collection<BpmNotification> getNotificationsForGrouping(int interval)
	{
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        int time = 1000*(c.get(Calendar.HOUR_OF_DAY) * 3600 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND));

		return (List<BpmNotification>)getSession()
				.createCriteria(BpmNotification.class)
                .add(Restrictions.eq("groupNotifications", true))
				.add(Restrictions.le("sendAfterHour", time+interval))
                .add(Restrictions.ge("sendAfterHour", time-interval))
				.setLockMode(LockMode.UPGRADE_NOWAIT)
				.addOrder(Order.asc("recipient"))
				.list();
	}
	/** Get all notifications properties */
	public static Collection<BpmNotificationMailProperties> getNotificationMailProperties()
	{
		Session session = getSession();

		return session.createCriteria(BpmNotificationMailProperties.class).list();
	}

	/** Saves given notifications to database */
	public static void addNotificationToBeSent(BpmNotification notification)
	{
		Session session = getSession();
		
		session.saveOrUpdate(notification);
	}
	
	public static void removeNotification(BpmNotification notification) 
	{
		Session session = getSession();
		
		session.delete(notification);
		
	}
	
	private static Session getSession()
	{
		return ProcessToolContext.Util.getThreadProcessToolContext().getHibernateSession();
	}



}
