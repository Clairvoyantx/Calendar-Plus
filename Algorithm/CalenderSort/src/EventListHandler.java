import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.*;


public class EventListHandler{


	private StaticEventList staticList;
	private DynamicEventList dynamicList;
	private ArrayList<CalendarEvent> events; //list to store all events in one given day


	public EventListHandler() {}

	public ArrayList<CalendarEvent> getEvents() {
		return events;
	}

	public void setEvents(ArrayList<CalendarEvent> events) {
		this.events = events;
	}

	public CalendarEvent getEventById (long Id) throws CalendarError {

        ArrayList<StaticEvent> staticArrayList = staticList.getList();
        if (staticArrayList != null) {
            for (int i = 0; i < staticArrayList.size(); i++) {
                if (staticArrayList.get(i).getId() == Id) {
                    return staticArrayList.get(i);
                }
            }
        }
        ArrayList<DynamicEvent> dynamicArrayList = dynamicList.getList();
        if (dynamicArrayList != null) {
            for (int i = 0; i < dynamicArrayList.size(); i++) {
                if (dynamicArrayList.get(i).getId() == Id) {
                    return dynamicArrayList.get(i);
                }
            }
        }
        
        return null;
	}
	
	public void initStaticList(){
		staticList = new StaticEventList();
	}

	public void initDynamicList(){
		dynamicList = new DynamicEventList();
	}

	public StaticEventList getStaticList() {
		return staticList;
	}

	public void setStaticList(StaticEventList staticList) {
		this.staticList = staticList;
	}

	public DynamicEventList getDynamicList() {
		return dynamicList;
	}

	public void setDynamicList(DynamicEventList dynamicList) {
		this.dynamicList = dynamicList;
	}


	public boolean checkValidTime(Calendar startTime, Calendar endTime){
		if (startTime.compareTo(endTime) >=0){
			return false;
		}
		return true;
	}

	//Create a static event to add to the static event list
	public boolean createStaticEvent(String name, String location, Calendar startTime, Calendar endTime,
			boolean isStatic, boolean isPeriodic, boolean isFinished, String description, String color) throws CalendarError{
		//check if start and end times are valid
		boolean check = false;
		if(!checkValidTime(startTime, endTime)){
			System.out.println("Fail");
			return false;
		}
		//check if event is static
		if (isStatic == false)
			return false;

		StaticEvent staticEvent = new StaticEvent(name, location, startTime, endTime, isStatic, 
				isPeriodic, isFinished, description, color);
		staticEvent.setId(System.currentTimeMillis());
		check = staticList.addEvent(staticEvent);
		if(!check)
			System.out.println("Fail");
		return (check);
	}



	public boolean removeEventById(int temp) throws CalendarError{
		boolean check = true;
		if (staticList == null){
			check = false;
			return check;
		}
		check = staticList.removeEventById(temp);
		return check;
	};

	//Create a dynamic event to add to the dynamic event list
	public void createDynamicEvent(String name, int estimatedLength, boolean isStatic,
			Calendar deadline, boolean isFinished, String description){
		return;
	}



	//Dynamic sort algorithm
	public boolean dynamicSort(){

		Comparator<StaticEvent> staticcomparator = new Comparator<StaticEvent>(){

			@Override
			public int compare(StaticEvent o1, StaticEvent o2) {
				return o1.getStartTime().compareTo(o2.getStartTime());
			}
		};

		Comparator<DynamicEvent> comparator = new Comparator<DynamicEvent>(){

			@Override
			public int compare(DynamicEvent o1, DynamicEvent o2) {
				return o1.getDeadline().compareTo(o2.getDeadline());
			}
		};

		Comparator<DynamicEvent> reversecomparator = new Comparator<DynamicEvent>(){

			@Override
			public int compare(DynamicEvent o1, DynamicEvent o2) {
				return -o1.getDeadline().compareTo(o2.getDeadline());
			}
		};

		PriorityQueue<DynamicEvent> currDynamicEList = new PriorityQueue<DynamicEvent>(comparator);
		PriorityQueue<DynamicEvent> reverseDynamicEList = new PriorityQueue<DynamicEvent>(reversecomparator);
		PriorityQueue<StaticEvent> currStaticEList = new PriorityQueue<StaticEvent>(staticcomparator);
		PriorityQueue<StaticEvent> sortedStaticEList = new PriorityQueue<StaticEvent>(staticcomparator);
		PriorityQueue<StaticEvent> freeList = new PriorityQueue<StaticEvent>(); 
		PriorityQueue<StaticEvent> sortedfreeList = new PriorityQueue<StaticEvent>(); 
		ArrayList<StaticEvent> staticArrayList = staticList.getList();
		ArrayList<DynamicEvent> dynamicArrayList = null;
		if(dynamicList!=null){
			dynamicArrayList = dynamicList.getList();
		}
		
		if(staticArrayList != null)
		{
			for(int i=0; i<staticArrayList.size();i++){
				if(!staticArrayList.get(i).isFinished()){
					currStaticEList.add(staticArrayList.get(i));
				}
			}}
		if(dynamicArrayList != null){
			for(int i=0; i<dynamicArrayList.size();i++){
				if(!dynamicArrayList.get(i).isFinished()){
					currDynamicEList.add(dynamicArrayList.get(i));
				}
			}}
		while (!currStaticEList.isEmpty()){
			DateFormat time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = currStaticEList.poll().getStartTime().getTime();
			System.out.println(time.format(date));
			
		}
		return false;
	}

	/*checks if there are conflicts in the static time table, if there are conflicts, return the two conflicted
	events as a single event*/
	private void checkConflict(PriorityQueue<StaticEvent> currStaticEList, PriorityQueue<StaticEvent> sortedStaticEList) 
			throws CalendarError{
		while(!currStaticEList.isEmpty()){
			StaticEvent firstCheck = currStaticEList.poll();
			StaticEvent secondCheck = null;
			if(!currStaticEList.isEmpty()){
				secondCheck = currStaticEList.peek();
			}
			if(firstCheck.getEndTime().compareTo(secondCheck.getStartTime()) >= 0){  //////////CONFIRM THIS or is it <=0
				StaticEvent newevent = new StaticEvent(firstCheck.getName(), 
						firstCheck.getLocation(), firstCheck.getStartTime(), secondCheck.getEndTime(),
						firstCheck.isStatic(), firstCheck.isPeriodic(), firstCheck.isFinished(),
						firstCheck.getDescription(), firstCheck.getColor());
				sortedStaticEList.add(newevent);
			}	
		}
	}
	
	//#1 on the order list
	//remove static time that are before and after 9a/p
	public void purgeStaticList(PriorityQueue<StaticEvent> sortedStaticEList){
		StaticEvent time;
		//create a calendar object with hour set to 9a/p
		Calendar endSet9 = Calendar.getInstance();
		endSet9.set(Calendar.HOUR_OF_DAY, 9);
		Calendar endSet21 = Calendar.getInstance();
		endSet21.set(Calendar.HOUR_OF_DAY, 21);
		
		while(!sortedStaticEList.isEmpty()){
			
			//if earlier than 9am set to 9am
			time = sortedStaticEList.peek();
			if(time.getEndTime().compareTo(endSet9) < 0){
				time.setEndTime(endSet9);
				continue;
			}
			
			//if later than 9pm set to 9pm
			else if(time.getEndTime().compareTo(endSet21) < 0){
				time.setEndTime(endSet21);
				continue;
			}
		}
	}
	
	private int daysBetween(Calendar d1, Calendar d2) {
        return (int) (Math.abs(d2.getTime().getTime() - d1.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }

	//#2 on the purge list
	//returns false if no free time, true will write freetime to freeList
	private boolean updateFreeTime(PriorityQueue<StaticEvent> sortedStaticEList,
			PriorityQueue<DynamicEvent> reverseDynamicEvent, PriorityQueue<StaticEvent> freeList) throws CalendarError{
		
		//get deadline from last of currDynamicEvent
		DynamicEvent lastdynamicevent = reverseDynamicEvent.peek();
		
		Calendar currTime = Calendar.getInstance();
		Calendar lastDynamicTime = lastdynamicevent.getEndTime();
		
		
		//make freeTime block
		Calendar startTime = null;
		Calendar endTime = null;
		StaticEvent freeBlock = null;
		int days = this.daysBetween(lastDynamicTime, currTime);
		for (int i=0; i<=days; i++){
			if (i==0){
				startTime = currTime;
			}

			else if(i==days){
				endTime = lastDynamicTime;
			}
			else {
			startTime = Calendar.getInstance();
			startTime.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,9, 0);
			endTime = Calendar.getInstance();
			endTime.set(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH,21, 0);
			}
			
			freeBlock = new StaticEvent("free time", "null", startTime, endTime,
			true, false, false, "null", "null");
			freeList.add(freeBlock);
			startTime.add(Calendar.DAY_OF_MONTH, 1);

		}
		
		//init freetime for the peek operation
		StaticEvent freetime;

		//this while loop while get all the free time blocks from available times and store
		//the free times in freeList, loop condition is while the sorted static event list is not empty keep going
		while(!sortedStaticEList.isEmpty())
		{
			//get temp as a staticEvent from the top of the sorted Q
			StaticEvent temp = sortedStaticEList.peek();
			//get freetime a staticEvent from the top of the freetime Q
			freetime = freeList.peek();
			
			//not enough time
			if(freetime == null)
				return false;
			
			//if the staticEvent's time is earlier the freeBlock's start, remove staticEvent from Q
			if (freetime.getStartTime().compareTo(temp.getEndTime()) >= 0){
				sortedStaticEList.poll();
				continue;
			}
			
			//if the static events starts before the freetime block but ends after
			else if(freetime.getStartTime().compareTo(temp.getStartTime()) > 0 && 
					(freetime.getStartTime()).compareTo(temp.getEndTime()) < 0){
				//set the start of the freeTime block to be the end of the static event
				freetime.setStartTime(temp.getEndTime());
				//remove the static event
				sortedStaticEList.poll();
				continue;		
			}
			
			//when we can actually start planning the freetime
			else if(freetime.getStartTime().compareTo(temp.getStartTime()) < 0){
				//this case we dont want because the event ends later than our freetime
				if(temp.getEndTime().compareTo(freetime.getEndTime()) > 0){
					sortedStaticEList.poll();
					continue;
				}
				//set start and end for the freetime to add to the Q
				temp.setStartTime(freetime.getStartTime());
				temp.setEndTime(temp.getStartTime());
				freeList.add(temp);
				//remove the static event to check from the Q
				sortedStaticEList.poll();
				//set starttime for next iteration
				freetime.setStartTime(temp.getEndTime());
				continue;
			}
			
			//if the start time of then static event if after the end of the free time block, remove static
			else if(freetime.getEndTime().compareTo(temp.getStartTime()) < 0){
				sortedStaticEList.poll();
				continue;
			}
		}
		return true;
	}
	
	//#3 on the order list
	//function to strip freetime of blocks less than 30min, do not remove 10 min for now
	public void purgefreeTime(PriorityQueue<StaticEvent> freeList, PriorityQueue<StaticEvent> sortedfreeList){
		StaticEvent freetime;
		while(!freeList.isEmpty()){
			freetime = freeList.peek();
			//confirm that this is indeed 30 min!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			if(freetime.getEndTime().compareTo(freetime.getStartTime()) < 30){
				freeList.poll();
				continue;
			}
			else{
				sortedfreeList.add(freetime);
				freeList.poll();
				continue;
			}
		}
	}
	
	
	
	
	


}

/*
ArrayList<StaticEvent>() events = EventListHandler.getStaticEventsByDateKey(string dateKey); ...DONE
ArrayList<DynamicEvent>() events = EventListHandler.getDynamicEventsByDateKey(string dateKey);
StaticEvent se;
string id = se.getId();
EventHandler.removeEventById(id); ...DONE

setEventFinished(String Id)
addColor field to staticEvent.... Done
 */