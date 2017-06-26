package EmailAPI;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail extends AsyncTask{

    private Context context;
    private Session session;

    private String emailAddress;
    private String subject;
    private String message;

    private ProgressDialog progressDialog;

    public SendMail(Context context, String emailAddress, String subject, String message){
        this.context = context;
        this.emailAddress = emailAddress;
        this.subject = subject;
        this.message = message;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(context, "Sending email...", "Please wait!", false, false);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        progressDialog.dismiss();
        Toast.makeText(context, "Email sent", Toast.LENGTH_LONG).show();
    }

    @Override
    protected Object doInBackground(Object[] params) {

        Properties props = new Properties();

        // Configurare proprietati GMAIL ----- A SE SCHIMBA IN FUNCTIE DE CLIENTUL DE MAIL
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailConfig.EMAIL_USERNAME, EmailConfig.EMAIL_PASSWORD);
            }
        });

        try{
            MimeMessage mm = new MimeMessage(session);
            mm.setFrom(new InternetAddress(EmailConfig.EMAIL_USERNAME));
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddress));
            mm.setSubject(subject);
            mm.setText(message);

            Transport.send(mm);

        }catch(MessagingException mEx){
            mEx.printStackTrace();
        }
        return null;
    }
}
