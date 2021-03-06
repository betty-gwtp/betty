package be.betty.gwtp.server.solver;

import java.util.HashSet;
import java.util.Set;

import net.sf.cpsolver.ifs.model.Constraint;
import net.sf.cpsolver.ifs.model.Model;

/**
 * Resource constraint
 * 
 * This class, like all the classes in this package are greatly inspired from the 
 * Amazing work of "Tomas Muller" for is "Iterative Forward Search".
 * His original Copyright has, of course, been removed, but it can be found in
 * the related doc.
 * 
 * <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 * <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 * <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see
 *          <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>.
 */
public class Resource extends Constraint<Activity, Location> {
    private String iName = null;
    private String iResourceId = null;
    private Activity[] iResource;
    private Set<Integer> iProhibitedSlots = new HashSet<Integer>();
    private Set<Integer> iDiscouragedSlots = new HashSet<Integer>();
    private int iType = TYPE_OTHER;
    
    public static final int TYPE_ROOM = 0;
    public static final int TYPE_INSTRUCTOR = 1;
    public static final int TYPE_CLASS = 2;
    public static final int TYPE_OTHER = 3;

    public Resource(String id, int type, String name) {
	super();
        iResourceId = id;
        iName = name;
        iType = type;
    }
    
    @Override
    public void setModel(Model<Activity, Location> model) {
        super.setModel(model);
        TimetableModel m = (TimetableModel)model;
        iResource = new Activity[m.getNrDays() * m.getNrHours()];
        for (int i=0;i<iResource.length;i++)
                iResource[i] = null;
    }
    
    public String getResourceId() { return iResourceId; }
    @Override
	public String getName() { return iName; }
    public int getType() { return iType; }
    public Set<Integer> getProhibitedSlots() { return iProhibitedSlots; }
    public Set<Integer> getDiscouragedSlots() { return iDiscouragedSlots; }
    public void addProhibitedSlot(int day, int hour) {
        iProhibitedSlots.add(((TimetableModel)getModel()).getNrHours()*day+hour);
    }
    public void addDiscouragedSlot(int day, int hour) {
        iDiscouragedSlots.add(((TimetableModel)getModel()).getNrHours()*day+hour);
    }
    public boolean isProhibitedSlot(int day, int hour) {
        return iProhibitedSlots.contains(((TimetableModel)getModel()).getNrHours()*day+hour);
    }
    public boolean isDiscouragedSlot(int day, int hour) {
        return iDiscouragedSlots.contains(((TimetableModel)getModel()).getNrHours()*day+hour);
    }
    public void addProhibitedSlot(int slot) {
        iProhibitedSlots.add(slot);
    }
    public void addDiscouragedSlot(int slot) {
        iDiscouragedSlots.add(slot);
    }
    public boolean isProhibitedSlot(int slot) {
        return iProhibitedSlots.contains(slot);
    }
    public boolean isDiscouragedSlot(int slot) {
        return iDiscouragedSlots.contains(slot);
    }    
    public boolean isProhibited(int day, int hour, int length) {
        int slot = ((TimetableModel)getModel()).getNrHours()*day+hour;
        for (int i=0;i<length;i++)
            if (iProhibitedSlots.contains(slot+i)) return true;
        return false;
    }
    
    @Override
	public void computeConflicts(Location location, Set<Location> conflicts) {
        Activity activity = location.variable();
        if (!location.containResource(this)) return;
        for (int i=location.getSlot(); i<location.getSlot()+activity.getLength(); i++) {
            Activity conf = iResource[i];
            if (conf!=null && !activity.equals(conf)) 
		conflicts.add(conf.getAssignment());
        }
    }
    
    @Override
	public boolean inConflict(Location location) {
        Activity activity = location.variable();
        if (!location.containResource(this)) return false;
        for (int i=location.getSlot(); i<location.getSlot()+activity.getLength(); i++) {
            if (iResource[i]!=null) return true;
        }
        return false;
    }
        
    @Override
	public boolean isConsistent(Location l1, Location l2) {
        return !l1.containResource(this) || !l2.containResource(this) || !l1.hasIntersection(l2);
    }
    
    @Override
	public void assigned(long iteration, Location location) {
        super.assigned(iteration, location);
        Activity activity = location.variable();
        if (!location.containResource(this)) return;
        for (int i=location.getSlot(); i<location.getSlot()+activity.getLength(); i++) {
            iResource[i] = activity;
        }
    }
    @Override
	public void unassigned(long iteration, Location location) {
        super.unassigned(iteration, location);
        Activity activity = location.variable();
        if (!location.containResource(this)) return;
        for (int i=location.getSlot(); i<location.getSlot()+activity.getLength(); i++) {
            iResource[i] = null;
        }
    }

    @Override
    public String toString() {
    	return "RessourceName="+iName;
    }
  
}
