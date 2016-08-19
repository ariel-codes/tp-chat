package cliente.ui;

import cliente.negocios.Client;
import cliente.negocios.MessageHandler;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ClientUI extends javax.swing.JFrame {

    public ClientUI(Client c, final MessageHandler msgHandler) {
        initComponents();

        // Things to change text color.
        this.doc = jMessages.getStyledDocument();
        this.style = new SimpleAttributeSet();

        this.client = c;
        this.msgHandler = msgHandler;

        // Declared a new Anonimous Runnable to update the messages in the box.
        this.messageUpdaterThread = new Thread(() -> {
            while (true) {
                synchronized (msgHandler) {
                    try {
                        msgHandler.wait();

                        String newString = MessageHandler.getMessage();

                        StyleConstants.setForeground(style, Color.BLACK);
                        StyleConstants.setBackground(style, Color.WHITE);

                        doc.insertString(doc.getLength(), newString, style);

                        jMessages.setSelectionStart(jMessages.getText().length());
                        jMessages.setSelectionEnd(jMessages.getText().length());

                    } catch (InterruptedException e) {
                        return;
                    } catch (BadLocationException ex) {
                        Logger.getLogger(ClientUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        // Another new Anonimous Runnable to update the client list in the other box.
        this.updateOnlineClients = new Thread(() -> {
            while (true) {

                try {
                    client.requestOnlineClients();
                    jClients.setText(client.getOnlineClientAsString());
                    Thread.sleep(60000);
                } catch (InterruptedException e) {

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Connection to server got down!\nPlease, try logging in again!", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }

            }
        });

        // Start the threads...
        this.messageUpdaterThread.setName("Message-Updater-Thread");
        this.messageUpdaterThread.start();
        this.updateOnlineClients.setName("Online-Clients-Updater");
        this.updateOnlineClients.start();

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jMessages = new javax.swing.JTextPane();
        jSender = new javax.swing.JTextField();
        jScrollPane5 = new javax.swing.JScrollPane();
        jClients = new javax.swing.JTextPane();
        jSend = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jDisconnect = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jChangeNick = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat Client");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jMessages.setEditable(false);
        jScrollPane4.setViewportView(jMessages);

        jSender.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jSenderKeyReleased(evt);
            }
        });

        jClients.setEditable(false);
        jScrollPane5.setViewportView(jClients);

        jSend.setText("Envia");
        jSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jSendActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSender, javax.swing.GroupLayout.DEFAULT_SIZE, 752, Short.MAX_VALUE)
                    .addComponent(jScrollPane4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jSend, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addComponent(jScrollPane5))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSend))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSender)
                        .addGap(4, 4, 4)))
                .addContainerGap())
        );

        jMenu1.setText("File");

        jDisconnect.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jDisconnect.setText("Disconnect");
        jDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDisconnectActionPerformed(evt);
            }
        });
        jMenu1.add(jDisconnect);

        jMenuBar1.add(jMenu1);

        jMenu3.setText("Opções");

        jChangeNick.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
        jChangeNick.setText("Change Nick");
        jChangeNick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jChangeNickActionPerformed(evt);
            }
        });
        jMenu3.add(jChangeNick);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void jSenderKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jSenderKeyReleased
        // TODO add your handling code here:
        try {

            if (!jSender.getText().startsWith("\n")) {
                switch (evt.getKeyCode()) {
                    // if the key is the up arrow, puts on the field the last thing sent.
                    case KeyEvent.VK_ENTER:
                        // If the release key is enter, just call the DecodeUserMessage method...

                        this.lastMessage = jSender.getText().trim();
                        String newMessage = this.client.DecodeUserMessage(this.lastMessage);
                        if (newMessage.startsWith("[ERROR")) {
                            StyleConstants.setForeground(style, Color.RED);
                            StyleConstants.setBackground(style, Color.WHITE);
                        } else {
                            StyleConstants.setForeground(style, Color.ORANGE);
                            StyleConstants.setBackground(style, Color.WHITE);
                        }
                        doc.insertString(doc.getLength(), newMessage, style);
                        this.jSender.setText(null);
                        break;
                    case KeyEvent.VK_UP:
                        this.jSender.setText(this.lastMessage);
                        break;
                    // just nothing...
                    default:
                        break;
                }
            } else {

            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Connection to server got down!\nPlease, try logging in again!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (BadLocationException ex) {
            Logger.getLogger(ClientUI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_jSenderKeyReleased


    private void jSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSendActionPerformed
        // TODO add your handling code here:

        try {

            this.lastMessage = jSender.getText().trim();
            this.client.DecodeUserMessage(this.lastMessage);
            jSender.setText(null);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Connection to server got down!\nPlease, try logging in again!", "Error", JOptionPane.ERROR_MESSAGE);

        }

    }//GEN-LAST:event_jSendActionPerformed

    private void jDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDisconnectActionPerformed
        // TODO add your handling code here:

        try {
            this.client.sendGoodbye();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Connection to server got down!\nPlease, try logging in again!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        this.dispose();

    }//GEN-LAST:event_jDisconnectActionPerformed


    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        try {
            this.client.sendGoodbye();
        } catch (IOException ex) {
            System.exit(1);
        }

        this.setVisible(false);

    }//GEN-LAST:event_formWindowClosing


    private void jChangeNickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jChangeNickActionPerformed
        // TODO add your handling code here:

        String newNick = JOptionPane.showInputDialog(this, "Please, type a new nick:\n", "Change Nickname", JOptionPane.QUESTION_MESSAGE);

        if (!newNick.equalsIgnoreCase("")) {
            try {
                this.client.changeNickName(newNick);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Connection to server got down.\nPlease, try logging in again!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nick has not been changed!", "Change Nick", JOptionPane.INFORMATION_MESSAGE);
        }

    }//GEN-LAST:event_jChangeNickActionPerformed

//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(ClientUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(ClientUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(ClientUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(ClientUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new ClientUI(null).setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jChangeNick;
    private javax.swing.JTextPane jClients;
    private javax.swing.JMenuItem jDisconnect;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JTextPane jMessages;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JButton jSend;
    private javax.swing.JTextField jSender;
    // End of variables declaration//GEN-END:variables

    private Client client;
    private MessageHandler msgHandler;

    private final Thread messageUpdaterThread;
    private Thread clientThread;
    private final Thread updateOnlineClients;

    private String lastMessage;

    private StyledDocument doc;
    private SimpleAttributeSet style;
}
