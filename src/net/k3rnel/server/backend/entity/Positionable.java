package net.k3rnel.server.backend.entity;

import net.k3rnel.server.backend.map.ServerMap;

import org.simpleframework.xml.Root;

/**
 * Provides an interface for game objects that can be placed on a map
 * @author shadowkanji
 *
 */
@Root
public interface Positionable {
	public enum Direction { Up, Down, Left, Right }
	public int getX();
	public int getY();
	public void setX(int x);
	public void setY(int y);
	public void setId(int id);
	public int getId();

	public String getName();
	public void setName(String name);
	public int getSprite();

	public boolean move(Direction d);
	public Direction getNextMovement();
	public int getPriority();
	public void queueMovement(Direction d);
	public Direction getFacing();
	public ServerMap getMap();

	public void setSprite(int sprite);

	public int getMapX();
	public int getMapY();
	
	public void setMap(ServerMap map, Direction dir);
	public void setVisible(boolean visible);
	
	public boolean isVisible();
}
