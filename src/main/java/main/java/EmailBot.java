package main.java;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class EmailBot {

    private static final String USERNAME    = "rchung@team4099.com";
    private static final String PASSWORD    = "tsvf gknm opum emcu";
    private static final String SUBJECT     = "Robotics Summer Camp Opportunity";
    private static final String SENDER_NAME = "Ryan Chung";
    private static final String TIME_OF_DAY = "afternoon";

    private static final List<String> INLINE_ATTACHMENTS = Arrays.asList(
            "Robocamps_Flyer_2025.png"
    );

    private static final List<String> FILE_ATTACHMENTS = Arrays.asList(
            "Robocamps_Flyer_2025.png"
    );

    public static void main(String[] args) {
        List<String[]> schools = readSchoolsFromCSV("schools.csv");

        for (String[] school : schools) {
            String schoolName = school[0];
            String email      = school[1];
            String htmlBody   = getEmailBody(schoolName, SENDER_NAME, TIME_OF_DAY);
            sendEmail(email, SUBJECT, htmlBody, INLINE_ATTACHMENTS, FILE_ATTACHMENTS);
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
        return "<!DOCTYPE html><html><body style='font-family:Arial,sans-serif;font-size:14px;'>"

                + "<p>Good " + timeOfDay + ",</p>"

                + "<p>My name is <strong>" + senderName + "</strong> and I'm a member of FIRST Robotics Team 4099, "
                + "a student-led team based in Poolesville, MD. We're extremely excited to introduce our "
                + "<strong>RoboCamps</strong> summer program! "
                + "Every summer, we hold this week-long camp at Poolesville Self Defense to help students ages 8–14 "
                + "explore computer science and engineering through hands-on instruction.</p>"

                + "<p>Over the course of a week, campers work in small groups to design, build, and program their own "
                + "robot using materials from the VEX IQ Ecosystem. This culminates in a final competition at the end "
                + "where campers can show off their robots and earn special prizes!</p>"

                + "<p>We're reaching out to you because we wanted to know if you would be willing to help us advertise "
                + "our program to your students at <strong>" + schoolName + "</strong>. "
                + "More information can be found on the RoboCamps page of our website. "
                + "In the past, schools have sent announcements or parent emails, but we are open to anything!</p>"

                + "<p><img src='cid:Robocamps_Flyer_2025.png' "
                + "     alt='RoboCamps Flyer' style='max-width:600px;width:100%;'/></p>"

                + "<p>If you have any questions or concerns, feel free to reply to this email or contact the team at "
                + "<a href='mailto:robocamps@team4099.com'>robocamps@team4099.com</a>.</p>"

                + "<p>Thank you so much for your time and consideration!</p>"

                + "<p>" + senderName + "</p>"

                + "<hr/>"
                + "<p><strong>Sample Announcement:</strong><br/>"
                + "FIRST Robotics Team 4099 is hosting RoboCamps this year! RoboCamps is a week-long summer camp held "
                + "at Poolesville Self Defense where students, ages 8–14, learn how to design, build, and code their "
                + "own robot. This interactive camp culminates in a final competition on the last day, where campers "
                + "can show off their robots and compete for a special prize! "
                + "<a href='https://team4099.com/robocamps'>Click here</a> to take a look at our program's website. "
                + "If you or someone you know might be interested, registration is open! "
                + "Email <a href='mailto:robocamps@team4099.com'>robocamps@team4099.com</a> with any questions. "
                + "We hope to see you there!</p>"

                + "</body></html>";
    }

    public static void sendEmail(String to,
                                 String subject,
                                 String htmlBody,
                                 List<String> inlineFiles,
                                 List<String> attachedFiles) {

        final String CC_EMAIL = "robocamps@team4099.com";

        Properties props = new Properties();
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL));
            message.setSubject(subject);

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8");

            MimeMultipart relatedPart = new MimeMultipart("related");
            relatedPart.addBodyPart(htmlPart);

            for (String filePath : inlineFiles) {
                MimeBodyPart inlinePart = new MimeBodyPart();
                DataSource ds = new FileDataSource(filePath);
                inlinePart.setDataHandler(new DataHandler(ds));
                String fileName = new File(filePath).getName();
                inlinePart.setHeader("Content-ID", "<" + fileName + ">");
                inlinePart.setDisposition(MimeBodyPart.INLINE);
                inlinePart.setFileName(fileName);
                relatedPart.addBodyPart(inlinePart);
            }

            MimeBodyPart relatedWrapper = new MimeBodyPart();
            relatedWrapper.setContent(relatedPart);

            MimeMultipart mixedPart = new MimeMultipart("mixed");
            mixedPart.addBodyPart(relatedWrapper);

            for (String filePath : attachedFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();
                DataSource ds = new FileDataSource(filePath);
                attachPart.setDataHandler(new DataHandler(ds));
                attachPart.setFileName(new File(filePath).getName());
                attachPart.setDisposition(MimeBodyPart.ATTACHMENT);
                mixedPart.addBodyPart(attachPart);
            }

            message.setContent(mixedPart);
            Transport.send(message);

            System.out.println("Email sent to: " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}