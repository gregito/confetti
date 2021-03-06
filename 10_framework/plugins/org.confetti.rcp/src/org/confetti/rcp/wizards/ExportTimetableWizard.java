package org.confetti.rcp.wizards;

import static com.google.common.collect.Lists.newArrayList;
import static org.confetti.rcp.wizards.ExportTimetableWizard.PrintersToHTML.MATRIX;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.confetti.core.Assignment;
import org.confetti.core.DataProvider;
import org.confetti.core.Day;
import org.confetti.core.Entity;
import org.confetti.core.Hour;
import org.confetti.core.Nameable;
import org.confetti.core.SolutionSlot;
import org.confetti.core.StudentGroup;
import org.confetti.core.Teacher;
import org.confetti.observable.ObservableList;
import org.confetti.rcp.ConfettiPlugin;
import org.confetti.rcp.wizards.models.ExportTimetableModel;
import org.confetti.rcp.wizards.pages.FolderChooseWizardPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * @author Gabor Bubla
 */
public class ExportTimetableWizard extends Wizard {

    public interface PrintToHTML {
        void print(PrintStream out, List<String> days, List<String> hours, String name, List<List<String>> timetable);
    }
    
    public enum PrintersToHTML implements PrintToHTML {
        MATRIX {
            @Override
            public void print(PrintStream out, List<String> days, List<String> hours, String name, List<List<String>> timetable) {
                out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\nhttp://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
                out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
                out.println();
                
                out.println("<head>");
                out.println("\t<title>" + name + "</title>");
                out.println("</head>");
                out.println();
                
                out.println("<body>");
                out.println();
                
                out.println("\t<table border=\"1\">");
                out.println("\t\t<caption>" + name + "</caption>");
                out.println("\t\t<thead>");
                out.println("\t\t\t<tr>");
                out.println("\t\t\t\t<th></th>");
                for (String day : days) {
                    out.println("\t\t\t\t<th>" + day + "</th>");
                }
                out.println("\t\t\t</tr>");
                out.println("\t\t</thead>");
                out.println("\t\t<tbody>");
                int hourCounter = 0;
                for (List<String> hour : timetable) {
                    out.println("\t\t\t<tr>");
                    out.println("\t\t\t\t<th>" + hours.get(hourCounter++) + "</th>");
                    for (String day : hour) {
                        out.println("\t\t\t\t<td>" + day + "</td>");
                    }
                    out.println("\t\t\t</tr>");
                }
                out.println("\t\t</tbody>");
                out.println("\t</table>");
                
                out.println();
                out.println("</body>");
                out.println();
                
                out.println("</html>");
            }
        };

    }
    
    private ExportTimetableModel model;

    public ExportTimetableWizard() {
        model = new ExportTimetableModel(null);
        setWindowTitle("Export timetables");
    }
    
    @Override
    public void addPages() {
        addPage(new FolderChooseWizardPage(model));
    }
    
    @Override
    public boolean performFinish() {
        try {
            File folderPath = new File(model.getFolderPath(), "timetables");
            folderPath.mkdir();
            exportTimetables(folderPath);
        } catch (IOException e) {
            MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", "Could not create the files\n\n" + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    private void exportTimetables(File folderPath) throws IOException {
        //get DataProvider's days, hours
        DataProvider dp = ConfettiPlugin.getDefault().getDataProvider().getValue();
        List<String> days = newArrayList(Iterables.transform(dp.getDays().getList(), new Function<Day, String>() {
            @Override
            public String apply(Day day) {
                return day.getName().getValue();
            }
        }));
        List<String> hours = newArrayList(Iterables.transform(dp.getHours().getList(), new Function<Hour, String>() {
            @Override
            public String apply(Hour hour) {
                return hour.getName().getValue();
            }
        }));
        
        //get the solution slots
        Map<Assignment, SolutionSlot> assignmentSolutionSlot = new HashMap<>();
        for (SolutionSlot slot : dp.getSolution().getValue()) {
            assignmentSolutionSlot.put(slot.getAssignment(), slot);
        }
        
        //get the teachers' and student groups' unsorted solution slots 
        List<Teacher> teachersList = newArrayList(dp.getTeachers().getList());
        List<StudentGroup> studentGroupsList = newArrayList(dp.getStudentGroups().getList());
        Map<Teacher, List<SolutionSlot>> teachersTimetable = new HashMap<>();
        Map<StudentGroup, List<SolutionSlot>> studentGroupsTimetable = new HashMap<>();
        for (Teacher teacher : teachersList) {
            List<SolutionSlot> solutionSlots = new ArrayList<>();
            for (Assignment assignment : teacher.getAssignments().getList()) {
                SolutionSlot solutionSlot = assignmentSolutionSlot.get(assignment);
                solutionSlots.add(solutionSlot);
            }
            teachersTimetable.put(teacher, solutionSlots);
        }
        for (StudentGroup studentGroup : studentGroupsList) {
            List<SolutionSlot> solutionSlots = new ArrayList<>();
            for (Assignment assignment : studentGroup.getAssignments().getList()) {
                SolutionSlot solutionSlot = assignmentSolutionSlot.get(assignment);
                solutionSlots.add(solutionSlot);
            }
            studentGroupsTimetable.put(studentGroup, solutionSlots);
        }
        
        //sort the solution slots and put empty string in empty slots
        export(folderPath, days, hours, teachersTimetable, studentGroupsTimetable);
    }
    
    private void export(File folderPath, List<String> days, List<String> hours, Map<Teacher, List<SolutionSlot>> teachersTimetable,
            Map<StudentGroup, List<SolutionSlot>> studentGroupsTimetable
    ) throws IOException {
        File teachersFolder = new File(folderPath, "teachers");
        File studentGroupsFolder = new File(folderPath, "studentgroups");
        teachersFolder.mkdir();
        studentGroupsFolder.mkdir();
        
        for (Map.Entry<Teacher, List<SolutionSlot>> entry : teachersTimetable.entrySet()) {
            String teacherName = entry.getKey().getName().getValue();
            List<List<String>> teacherTimetable = createEmptyTimeTable(hours.size(), days.size());
            for (SolutionSlot solutionSlot : entry.getValue()) {
                        teacherTimetable.get(hours.indexOf(solutionSlot.getHour().getName().getValue())).set(days.indexOf(solutionSlot.getDay().getName().getValue()),
                        solutionSlot.getAssignment().getSubject().getName().getValue()
                        + "<br />"
                        + getNames(solutionSlot.getAssignment().getStudentGroups()));
                
            }
            try (PrintStream out = new PrintStream(new File(teachersFolder, teacherName + ".html"))) {
                MATRIX.print(out, days, hours, teacherName, teacherTimetable);
            }
        }
        for (Map.Entry<StudentGroup, List<SolutionSlot>> entry : studentGroupsTimetable.entrySet()) {
            String studentGroupName = entry.getKey().getName().getValue();
            List<List<String>> studentGroupTimetable = createEmptyTimeTable(hours.size(), days.size());
            for (SolutionSlot solutionSlot : entry.getValue()) {
                studentGroupTimetable.get(hours.indexOf(solutionSlot.getHour().getName().getValue())).set(days.indexOf(solutionSlot.getDay().getName().getValue()),
                        solutionSlot.getAssignment().getSubject().getName().getValue()
                        + "<br />"
                        + getNames(solutionSlot.getAssignment().getTeachers()));
                
            }
            try (PrintStream out = new PrintStream(new File(studentGroupsFolder, studentGroupName + ".html"))) {
                MATRIX.print(out, days, hours, studentGroupName, studentGroupTimetable);
            }
        }
        exportToHTMLIndex(folderPath);
        exportToHTMLFrame(folderPath, teachersTimetable, studentGroupsTimetable);
    }
    
    private void exportToHTMLIndex(File folderPath) throws IOException {
        try (PrintStream out = new PrintStream(new File(folderPath, "index.html"))) {
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">");
            out.println("<html>");
            
            out.println("<head>");
            out.println("<title>Index</title>");
            out.println("</head>");
            
            out.println("<frameset cols=\"20%,80%\">");
            out.println("<frame src=\"overview-frame.html\">");
            out.println("<frame name=\"entityFrame\" scrolling=\"yes\">");
            out.println("</frameset");
            
            out.println("</html>");
        }
    }

    private void exportToHTMLFrame(File folderPath, Map<Teacher, List<SolutionSlot>> teachersTimetable,
            Map<StudentGroup, List<SolutionSlot>> studentGroupsTimetable
    ) throws IOException {
        try (PrintStream out = new PrintStream(new File(folderPath, "overview-frame.html"))) {
            out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
            out.println("<html>");
            
            out.println("<head>");
            out.println("<title>Overview list</title>");
            out.println("</head>");
            
            out.println("<body>");
            out.println("<h2>Teachers</h2>");
            out.println("<ul>");
            List<String> teacherNames = convertToNames(teachersTimetable.keySet());
            Collections.sort(teacherNames);
            for (String name : teacherNames) {
                out.println("<li><a href=\"teachers/" + name + ".html\" target=\"entityFrame\">" + name + "</a></li>");
            }
            out.println("</ul>");
            
            out.println("<h2>Student groups</h2>");
            out.println("<ul>");
            List<String> studentGroupNames = convertToNames(studentGroupsTimetable.keySet());
            Collections.sort(studentGroupNames);
            for (String name : studentGroupNames) {
                out.println("<li><a href=\"studentgroups/" + name + ".html\" target=\"entityFrame\">" + name + "</a></li>");
            }
            out.println("</ul>");
            out.println("</body>");
            
            out.println("</html>");
        }
    }

    private List<String> convertToNames(Set<? extends Entity> set) {
        List<String> names = newArrayList(Iterables.transform(set, new Function<Entity, String>() {
            @Override public String apply(Entity e) { return e.getName().getValue(); }
        }));
        return names;
    }

    private List<List<String>> createEmptyTimeTable(int hoursSize, int daysSize) {
        List<List<String>> emptyTimetable = new ArrayList<List<String>>();
        for (int i = 0; i < hoursSize; i++) {
            List<String> hoursList = new ArrayList<>();
            for (int j = 0; j < daysSize; j++) {
                hoursList.add("");
            }
            emptyTimetable.add(hoursList);
        }
        return emptyTimetable;
    }
    
    private static List<String> getNames(ObservableList<? extends Nameable> items) {
        List<String> names = new ArrayList<>();
        for (Nameable nameable : items.getList()) {
            names.add(nameable.getName().getValue());
        }
        return names;
    }

}
