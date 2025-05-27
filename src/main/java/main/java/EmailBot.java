package main.java;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.internet.InternetAddress;
import javax.mail.*;
import javax.mail.internet.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class EmailBot {

    private static final String USERNAME = ""; // Replace with your email
    private static final String PASSWORD = "";    // Use an App Password
    private static final String SUBJECT = "Robotics Summer Camp Opportunity";
    private static final String SENDER_NAME = "Ryan Chung";     //Add your name
    private static final String TIME_OF_DAY = "afternoon";
    private static final List<String> ATTACHMENTS = Arrays.asList("Robocamps Flyer 2025.png");

    public static void main(String[] args) {
        List<String[]> schools = readSchoolsFromCSV("schools.csv");

        for (String[] school : schools) {
            String schoolName = school[0];
            String email = school[1];
            String messageBody = getEmailBody(schoolName, SENDER_NAME, TIME_OF_DAY);
            sendEmail(email, SUBJECT, messageBody, ATTACHMENTS);
        }
    }

    public static List<String[]> readSchoolsFromCSV(String fileName) {
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            List<String[]> allRows = reader.readAll();
            allRows.remove(0);
            return allRows;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getEmailBody(String schoolName, String senderName, String timeOfDay) {
        return "Good " + timeOfDay + ",\n\n" +
                "My name is " + senderName + " and I’m a member of FIRST Robotics Team 4099, " +
                "a student-led team based in Poolesville, MD. We’re extremely excited to introduce our RoboCamps summer program! " +
                "Every summer, we hold this week-long camp at Poolesville Self Defense to help students ages 8–14 explore computer science " +
                "and engineering through hands-on instruction.\n\n" +
                "Over the course of a week, campers work in small groups to design, build, and program their own robot using materials " +
                "from the VEX IQ Ecosystem. This culminates in a final competition at the end where campers can show off their robots and earn special prizes!\n\n" +
                "We’re reaching out to you because we wanted to know if you would be willing to help us advertise our program to your students at " + schoolName + ". " +
                "More information about the opportunity can be found on the RoboCamps page of our website, and I’ve attached an announcement blurb and our flyer below. " +
                "In the past, schools have sent announcements or parent emails, but we are open to anything!\n\n" +
                "If you have any questions or concerns, feel free to reply to this email or contact the team at robocamps@team4099.com.\n\n" +
                "Thank you so much for your time and consideration!\n\n" +
                senderName + "\n\n" +
                "Sample Announcement:\n" +
                "FIRST Robotics Team 4099 is hosting RoboCamps this year! RoboCamps is a week-long summer camp held at Poolesville Self Defense " +
                "where students, ages 8–14, learn how to design, build, and code their own robot. This interactive camp culminates in a final competition " +
                "on the last day, where campers can show off their robots and compete for a special prize! Click here to take a look at our program’s website. " +
                "If you or someone you know might be interested in this program, registration is open! If you have any questions, feel free to email robocamps@team4099.com. " +
                "We hope to see you there!";
    }

    public static void sendEmail(String to, String subject, String body, List<String> filePaths) {
        final String CC_EMAIL = "robocamps@team4099.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL));
            message.setSubject(subject);

            Multipart multipart = new MimeMultipart();

            // Email body
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);
            multipart.addBodyPart(textPart);

            // Attachments
            for (String filePath : filePaths) {
                MimeBodyPart attachment = new MimeBodyPart();
                DataSource source = new FileDataSource(filePath);
                attachment.setDataHandler(new DataHandler(source));
                attachment.setFileName(new File(filePath).getName());
                multipart.addBodyPart(attachment);
            }

            message.setContent(multipart);
            Transport.send(message);

            System.out.println("Email sent to: " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

