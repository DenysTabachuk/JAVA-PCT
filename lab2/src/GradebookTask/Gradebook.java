package GradebookTask;

import java.util.ArrayList;
import java.util.List;

class Gradebook {
    private List<Group> groups;

    public Gradebook(int numberOfGroups) {
        groups = new ArrayList<>();
        for (int i = 0; i < numberOfGroups; i++) {
            groups.add(new Group("Group"+i+1));
        }
    }

    public Group getGroup(int groupNumber) {
        if (groupNumber >= 1 && groupNumber <= groups.size()) {
            return groups.get(groupNumber - 1);
        }
        return null;
    }

    public List<Group> getGroups() {
        return groups;
    }

}
