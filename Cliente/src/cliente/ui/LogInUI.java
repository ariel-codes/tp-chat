package cliente.ui;

import cliente.negocios.Client;
import cliente.negocios.MessageHandler;
import common.exceptions.EmptyFieldException;
import common.exceptions.InvalidNickName;
import common.exceptions.NickWithSpacesException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;


public class LogInUI extends javax.swing.JFrame {

    private Client client;
    private Thread clientThread;

    private MessageHandler msgHandler;
    private Thread msgHandlerThread;

    
    public LogInUI() {
        initComponents();
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jServerIP = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jServerPort = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jNickName = new javax.swing.JTextField();
        jConnect = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Chat Client");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setResizable(false);

        jLabel1.setText("IP:");

        jServerIP.setText("127.0.0.1");

        jLabel2.setText("Porta");

        jServerPort.setText("8885");

        jLabel3.setText("Nick:");

        jNickName.setText("Olivier");

        jConnect.setText("Conectar");
        jConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jConnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jConnect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jServerIP, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                            .addComponent(jNickName)
                            .addComponent(jServerPort))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jServerIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jServerPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jNickName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jConnect)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jConnectActionPerformed
        // TODO add your handling code here:

        try {

            // If some fields don't have nothing, throw the exception.
            if (jServerIP.getText().equalsIgnoreCase("") || jServerPort.getText().equalsIgnoreCase("") || jNickName.getText().equalsIgnoreCase("")) {
                throw new EmptyFieldException();
            }
            else if ( jNickName.getText().contains(" ") ){
                throw new NickWithSpacesException();
            }

            // Does a simple single ton to a client.
            if (this.client == null) {
                this.client = new Client(jServerIP.getText(), Integer.parseInt(jServerPort.getText()), jNickName.getText());
                this.clientThread = new Thread(this.client);
                this.clientThread.setName("Client-Thread");
                this.clientThread.start();
            }
            else {
                this.client.getConnected(this.jNickName.getText());
            }

            // Also a simple single ton, but now for the Message Handler.
            if (this.msgHandler == null) {
                this.msgHandler = new MessageHandler(this.client);
                this.msgHandlerThread = new Thread(this.msgHandler);
                this.msgHandlerThread.setName("Message-Handler-Thread");
                this.msgHandlerThread.start();
            }
            synchronized (msgHandler) {
                try {
                    msgHandler.wait(); // Waits for the server answer...
                    if (!client.isIDSetted()) // If the client is not connected, throws the InvalidNickName exception.
                        throw new InvalidNickName();
                    new ClientUI(this.client, this.msgHandler).setVisible(true);
                    this.dispose();

                } catch (InterruptedException ex) {
                    Logger.getLogger(LogInUI.class.getName()).log(Level.SEVERE, null, ex);

                } catch (InvalidNickName e) {
                    JOptionPane.showMessageDialog(this, "Invalid nickname! Someone is already using it!\nPlease, try another one.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }

        } catch (EmptyFieldException e) {
            JOptionPane.showMessageDialog(this, "Fill all the blanks!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Couldn't connect to server!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NickWithSpacesException e) {
            JOptionPane.showMessageDialog(this, "Please, do not use spaces in the nickname!", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_jConnectActionPerformed

    
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LogInUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new LogInUI().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jConnect;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jNickName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jServerIP;
    private javax.swing.JTextField jServerPort;
    // End of variables declaration//GEN-END:variables
}
