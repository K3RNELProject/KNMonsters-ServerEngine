package net.k3rnel.server;

import net.k3rnel.server.backend.MovementService;
import net.k3rnel.server.backend.SpriteList;
import net.k3rnel.server.backend.item.ItemDatabase;
import net.k3rnel.server.battle.DataService;
import net.k3rnel.server.feature.JythonService;
import net.k3rnel.server.feature.TimeService;
import net.k3rnel.server.network.IdleTimer;
import net.k3rnel.server.network.NetworkService;

/**
 * Handles all services on the game server
 * @author shadowkanji
 *
 */
public class ServiceManager {
	private NetworkService m_networkService;
	private MovementService m_movementService;
	private DataService m_dataService;
	private TimeService m_timeService;
	private JythonService m_jythonService;
	private IdleTimer m_idleTimer;
	private SpriteList m_sprites;
	private ItemDatabase m_itemdatabase;
	
	/**
	 * Default constructor
	 */
	public ServiceManager() {
		/*
		 * Initialize all the services
		 */
		m_jythonService = new JythonService();
		m_timeService = new TimeService();
		m_dataService = new DataService();
		m_networkService = new NetworkService();
		m_itemdatabase = new ItemDatabase();
		m_movementService = new MovementService();
		m_idleTimer = new IdleTimer();
		m_sprites = new SpriteList();
	}
	
	/**
	 * Returns the data service (contains battle mechanics, polrdb, etc.)
	 * @return
	 */
	public DataService getDataService() {
		return m_dataService;
	}
	
	/**
	 * Returns the movement service
	 * @return
	 */
	public MovementService getMovementService() {
		return m_movementService;
	}
	
	/**
	 * Returns the network service
	 * @return
	 */
	public NetworkService getNetworkService() {
		return m_networkService;
	}
	
	/**
	 * Returns the time service
	 * @return
	 */
	public TimeService getTimeService() {
		return m_timeService;
	}
	
	/**
	 * Returns the jython service
	 * @return
	 */
	public JythonService getJythonService() {
		return m_jythonService;
	}
	
	/**
	 * Returns the list of unbuyable sprites
	 * @return
	 */
	public SpriteList getSpriteList() {
		return m_sprites;
	}
	
	/**
	 * Starts all services
	 */
	public void start() {
		/*
		 * Start the network service first as it needs to bind the address/port to the game server.
		 * Then start all other services with TimeService last.
		 */
		m_sprites.initialise();
		m_itemdatabase.initialise();
		m_movementService.start();
		m_networkService.start();
		m_timeService.start();
		m_idleTimer.start();
		System.out.println("INFO: Service Manager startup completed.");
	}
	
	/**
	 * Stops all services
	 */
	public void stop() {
		/*
		 * Stopping services is very delicate and must be done in the following order to avoid
		 * leaving player objects in a non-concurrent state.
		 */
		m_idleTimer.stop();
		m_timeService.stop();
		m_movementService.stop();
		m_networkService.stop();
		System.out.println("INFO: Service Manager stopped.");
	}

	public ItemDatabase getItemDatabase() {
		return m_itemdatabase;
	}

	public void setItemDatabase(ItemDatabase mItemdatabase) {
		m_itemdatabase = mItemdatabase;
	}
}
