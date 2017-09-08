package highloadcup.service;

import highloadcup.entity.Location;
import highloadcup.entity.User;
import highloadcup.entity.Visit;
import highloadcup.server.ApiHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by d.asadullin on 17.08.2017.
 */
public class DataHolder {
    public static int OK_RESP = 200;
    public static int NOTFOUND_RESP = 404;
    public static int INCORRECT_RESP = 400;
    public static int WAIT = -1;
    public static Comparator<Visit> vComp = (o1, o2) -> o1.getVisited_at() > o2.getVisited_at() ? 1 : o1.getVisited_at() == o2.getVisited_at() ? 0 : -1;
    Logger logger = LoggerFactory.getLogger(DataHolder.class);
    private Random r = new Random();
    private Visit[] visits;
    private User[] users;
    private Location[] locations;

    private ReadWriteLock visitsLock = new ReentrantReadWriteLock();
    private ReadWriteLock usersLock = new ReentrantReadWriteLock();
    private ReadWriteLock locationsLock = new ReentrantReadWriteLock();

    public List<Visit> getUserVisits(int id) {
        if (id < users.length) {
            User user = users[id];
            if (user != null) {
                return user.getLst();
            }
        }
        return null;
    }

    public void removeFromUserVisits(int id, Visit visit) {
        if (id < users.length) {
            User user = users[id];
            if (user != null) {
                user.getLst().remove(visit);
            }
        }
    }

    public List<Visit> getLocationVisits(int id) {
        if (id < locations.length) {
            Location loc = locations[id];
            if (loc != null) {
                return loc.getLst();
            }
        }
        return null;
    }

    public void removeFromLocationVisits(int id, Visit visit) {
        if (id < locations.length) {
            Location loc = locations[id];
            if (loc != null) {
                loc.getLst().remove(visit);
            }
        }
    }

    public DataHolder() {
        visits = new Visit[12000000];
        users = new User[1200000];
        locations = new Location[1200000];
    }

    public Integer getRandomUserId() {
        User u = null;
        int i = 0;
        while ((u = users[r.nextInt(users.length - 1)]) == null && i++ < 100) ;
        return u != null ? u.getId() : r.nextInt(2000000);
    }

    public Integer getRandomLocationId() {
        Location u = null;
        int i = 0;
        while ((u = locations[r.nextInt(users.length - 1)]) == null && i++ < 100) ;
        return u != null ? u.getId() : r.nextInt(2000000);
    }

    public Integer getRandomVisitId() {
        Visit u = null;
        int i = 0;
        while ((u = visits[r.nextInt(visits.length - 1)]) == null && i++ < 100) ;
        return u != null ? u.getId() : r.nextInt(1000000);
    }

    public User[] getUserIds() {
        return users;
    }

    public Location[] getLocationsIds() {
        return locations;
    }

    public Visit[] getVisitsIds() {
        return visits;
    }

    public void printLastVisit() {
        int maxId = -1;
        for (int i = visits.length - 1; i > 0; i--) {
            if (visits[i] != null) {
                maxId = i;
                break;
            }
        }
        logger.info("Max visit id {}", maxId);
    }

    public void printLastLocation() {
        int maxId = -1;
        for (int i = locations.length - 1; i > 0; i--) {
            if (locations[i] != null) {
                maxId = i;
                break;
            }
        }
        logger.info("Max location id {}", maxId);
    }

    public void printLastUser() {
        int maxId = -1;
        for (int i = users.length - 1; i > 0; i--) {
            if (users[i] != null) {
                maxId = i;
                break;
            }
        }
        logger.info("Max users id {}", maxId);
    }


    public User getUser(int id) {
//        Lock lock = usersLock.readLock();
//        lock.lock();
//        try {
        if (id >= users.length) {
            return null;
        } else {
            return users[id];
        }
//        } finally {
//            lock.unlock();
//        }
    }


    public Location getLocation(int id) {
//        Lock lock = locationsLock.readLock();
//        lock.lock();
//        try {
        if (id >= locations.length) {
            return null;
        } else {
            return locations[id];
        }
//        } finally {
//            lock.unlock();
//        }
    }


    public Visit getVisit(int id) {
//        Lock lock = visitsLock.readLock();
//        lock.lock();
//        try {
        if (id >= visits.length) {
            return null;
        } else {
            return visits[id];
        }
//        } finally {
//            lock.unlock();
//        }
    }


    public VisitResponse getVisits(int id, Object[] params) {
        List<Visit> visits;
        Lock lock = usersLock.readLock();
        try {
            lock.lock();
            visits = getUserVisits(id);
        } finally {
            lock.unlock();
        }
        if (visits == null) {
            return new VisitResponse(NOTFOUND_RESP, "{\"visits\": [ ]}");
        }
        if (visits.isEmpty()) {
            return new VisitResponse(OK_RESP, "{\"visits\": [ ]}");
        }
        for (int i = 0; i < visits.size() - 1; i++) {
            if (visits.get(i).getVisited_at() > visits.get(i + 1).getVisited_at()) {
                Collections.sort(visits, vComp);
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"visits\": [ ");
        for (int i = 0; i < visits.size(); i++) {
            if(params!=null) {
                if (params[ApiHandler.from_date_idx] != null && (Long)params[ApiHandler.from_date_idx] >= visits.get(i).getVisited_at()) {
                    continue;
                }
                if (params[ApiHandler.to_date_idx] != null && (Long)params[ApiHandler.to_date_idx] <= visits.get(i).getVisited_at()) {
                    continue;
                }
                if (params[ApiHandler.county_idx] != null && !params[ApiHandler.county_idx].equals(visits.get(i).getLocationEntry().getCountry())) {
                    continue;
                }
                if (params[ApiHandler.distance_idx] != null && (Integer)params[ApiHandler.distance_idx]  <= visits.get(i).getLocationEntry().getDistance()) {
                    continue;
                }
            }
            visits.get(i).toString(sb);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]}");
        return new VisitResponse(OK_RESP, sb.toString());
    }


    public AvgResponse getAvgMark(int id, Object[] params) {
        List<Visit> visits;
        Lock lock = locationsLock.readLock();
        try {
            lock.lock();
            visits = getLocationVisits(id);
        } finally {
            lock.unlock();
        }
        if (visits == null) {
            return new AvgResponse(NOTFOUND_RESP, 0d);
        }

        if (visits.isEmpty()) {
            return new AvgResponse(OK_RESP, 0d);
        }

        double sum=0;
        double cnt=0;
        for (Visit v : visits) {
            if(params!=null) {
                if (params[ApiHandler.from_date_idx] != null && (Long)params[ApiHandler.from_date_idx] >= v.getVisited_at()) {
                    continue;
                }
                if (params[ApiHandler.to_date_idx] != null && (Long)params[ApiHandler.to_date_idx] <= v.getVisited_at()) {
                    continue;
                }
                if (params[ApiHandler.gender_idx] != null && !((Boolean) params[ApiHandler.gender_idx]).equals(v.getUserEntry().getGender())) {
                    continue;
                }
                if (params[ApiHandler.from_age_idx] != null && v.getUserEntry().after((Integer) params[ApiHandler.from_age_idx] )) {
                    continue;
                }
                if (params[ApiHandler.to_age_idx] != null && !v.getUserEntry().after((Integer) params[ApiHandler.to_age_idx])) {
                    continue;
                }
            }
            sum+= v.getMark();
            cnt++;
        }
        double res = 0;
        if (cnt>0) {
            res = sum / cnt;
        }
        return new AvgResponse(OK_RESP, res);
    }


    public Integer updateUser(int id, User user) {
        User old = getUser(id);
        if (old == null) {
            return NOTFOUND_RESP;
        }
        old.fromUser(user);
        old.refreshSource();
        return OK_RESP;
    }


    public Integer updateVisit(int id, Visit visit) {
        Visit old = getVisit(id);
        if (old == null) {
            return NOTFOUND_RESP;
        }
        int oldLocation = old.getLocation();
        int oldUser = old.getUser();
        User newUser = null;
        if (visit.getUser() >= 0) {
            newUser = getUser(visit.getUser());
            if (newUser == null) {
                return INCORRECT_RESP;
            }
        }
        Location newLocation = null;
        if (visit.getLocation() >= 0) {
            newLocation = getLocation(visit.getLocation());
            if (newLocation == null) {
                return INCORRECT_RESP;
            }

        }
        old.fromVisit(visit);
        if (newUser != null) {
            old.setUserEntry(newUser);
            if (old.getUser() != oldUser) {
                Lock lock = usersLock.writeLock();
                try {
                    lock.lock();
                    removeFromUserVisits(oldUser, old);
                    List<Visit> lst = newUser.getLst();
                    lst.add(old);
                    Collections.sort(lst, vComp);
                } finally {
                    lock.unlock();
                }
            }
        }
        if (newLocation != null) {
            old.setLocationEntry(newLocation);
            if (old.getLocation() != oldLocation) {
                Lock lock = locationsLock.writeLock();
                try {
                    lock.lock();
                    removeFromLocationVisits(oldLocation, old);
                    List<Visit> lst = newLocation.getLst();
                    lst.add(old);
                    Collections.sort(lst, vComp);

                } finally {
                    lock.unlock();
                }
            }
        }
        return OK_RESP;
    }


    public Integer updateLocation(int id, Location location) {
        Location old = getLocation(id);
        if (old == null) {
            return NOTFOUND_RESP;
        }
        old.fromLocation(location);
        old.refreshSource();
        return OK_RESP;
    }

    public Integer addUser(User user) {
        while (user.getId() >= users.length) {
            Lock lock = usersLock.writeLock();
            lock.lock();
            try {
                growUsers();
            } finally {
                lock.unlock();
            }
        }
        if (users[user.getId()] != null) {
            return INCORRECT_RESP;
        }
        Lock lock = usersLock.writeLock();
        lock.lock();
        try {
            users[user.getId()] = user;
        } finally {
            lock.unlock();
        }
        user.refreshSource();
        //  logger.info("add "+user.toString());
        return OK_RESP;
    }

    public Integer addLocation(Location location) {
        while (location.getId() >= locations.length) {
            Lock lock = locationsLock.writeLock();
            lock.lock();
            try {
                growLocations();
            } finally {
                lock.unlock();
            }
        }
        if (locations[location.getId()] != null) {
            return INCORRECT_RESP;
        }

        Lock lock = locationsLock.writeLock();
        lock.lock();
        try {
            locations[location.getId()] = location;
        } finally {
            lock.unlock();
        }

        location.refreshSource();
        //  logger.info("add "+user.toString());
        return OK_RESP;
    }

    public Integer addVisit(Visit visit) {
        User user = visit.getUser() >= 0 ? getUser(visit.getUser()) : null;
        Location location = visit.getLocation() >= 0 ? getLocation(visit.getLocation()) : null;
        if (user == null || location == null) {
            return INCORRECT_RESP;
        }

        while (visit.getId() >= visits.length) {
            Lock lock = visitsLock.writeLock();
            lock.lock();
            try {
                growVisits();
            } finally {
                lock.unlock();
            }
        }
        if (visits[visit.getId()] != null) {
            return INCORRECT_RESP;
        }

        Lock lock = visitsLock.writeLock();
        lock.lock();
        try {
            visits[visit.getId()] = visit;
        } finally {
            lock.unlock();
        }
        visit.setUserEntry(user);
        visit.setLocationEntry(location);
        Lock lock1 = usersLock.writeLock();
        try {
            lock1.lock();
            List<Visit> lst = user.getLst();
            lst.add(visit);
            Collections.sort(lst, vComp);
        } finally {
            lock1.unlock();
        }
        Lock lock2 = locationsLock.writeLock();
        try {
            lock2.lock();
            List<Visit> lst = location.getLst();
            lst.add(visit);
            Collections.sort(lst, vComp);
        } finally {
            lock2.unlock();
        }
        return OK_RESP;
    }

    private void growVisits() {
        int oldCapacity = visits.length;
        int newCapacity = oldCapacity + 100000;
        logger.info("grow visits to {}" + newCapacity);
        visits = Arrays.copyOf(visits, newCapacity);
    }

    private void growUsers() {
        int oldCapacity = users.length;
        int newCapacity = oldCapacity + 100000;
        logger.info("grow users to {}" + newCapacity);
        users = Arrays.copyOf(users, newCapacity);
    }
    private void growLocations() {
        int oldCapacity = locations.length;
        int newCapacity = oldCapacity + 100000;
        logger.info("grow locations to {}" + newCapacity);
        locations = Arrays.copyOf(locations, newCapacity);
    }
}
