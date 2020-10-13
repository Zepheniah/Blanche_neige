// -*- coding: utf-8 -*-
package com.company;

import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;

public class SeptNains {
    static private SimpleDateFormat sdf = new SimpleDateFormat("hh'h 'mm'mn 'ss','SSS's'");

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        Date début = new Date(start);
        long fin = start+5000; // Début de programme + 5 seconde, variable permettant de mettre fin au programme au bout de 5 seconde

        System.out.println("[" + sdf.format(début) + "] Début du programme.");

        final BlancheNeige bn = new BlancheNeige();
        final int nbNains = 7;
        final String noms [] = {"Simplet", "Dormeur",  "Atchoum", "Joyeux", "Grincheux",
                "Prof", "Timide"};
        final Nain nain [] = new Nain [nbNains];
        for(int i = 0; i < nbNains; i++) nain[i] = new Nain(noms[i],bn);
        for (int i = 0; i < nbNains; i++) nain[i].start();


        while (System.currentTimeMillis()<fin); // Patiente 5 seconde avant de commencer la fermeture du programme


        System.out.println("Interruption des 7 nains.");
        /**
         * Interrompt chaque thread de nain
         */
        for (int i = 0; i < nbNains; i++){
            nain[i].interrupt();
        }

        /**
         * Permet de vérifier que bel et bien tout les threads de nain soit interrompu
         * NB : Pour une raison que j'ignore parfois aucun thread ne se retrouve interrompu empechant le programme de sortir de la boucle while
         */
        boolean terminated = true;
        while (terminated){
            for (int i = 0; i < nbNains; i++){
                if(!nain[i].isInterrupted()) terminated = true;
                else terminated = false;
            }
        }
        Thread.yield();
        if (!terminated)System.out.println(new Date(System.currentTimeMillis())+" "+"Tous les nains ont terminé.");
    }


    }


class BlancheNeige {
    private volatile boolean libre = true;        // Initialement, Blanche-Neige est libre.
    private volatile LinkedList<String> FIFO = new LinkedList<>(); // Liste qui sera utilisé comme file d'attente

    public synchronized void requérir () throws InterruptedException {

        /**
         * Je crée une liste avec les nains et si un nain est pas en tête de liste
         * alors il patientera que son tour arrive
         */
        FIFO.add("Simplet");
        FIFO.add("Dormeur");
        FIFO.add("Atchoum");
        FIFO.add("Joyeux");
        FIFO.add("Grincheux");
        FIFO.add("Prof");
        FIFO.add("Timide");

        System.out.println("\t" + Thread.currentThread().getName()
                + " veut la ressource.");
        while (Thread.currentThread().getName() != FIFO.peekFirst()){ // Si le thread courant n'est pas premier dans la file alors wait
                wait();
        }

    }

    public synchronized void accéder () throws InterruptedException {

        while(!libre)wait();                    // Le nain s'endort sur l'objet bn
        libre = false;
        System.out.println("\t" + Thread.currentThread().getName()
                + " accède à la ressource.");
    }

    public synchronized void relâcher (){
        System.out.println("\t" + Thread.currentThread().getName()
                + " relâche la ressource.");
        /**
         * Je prends la tete de file qui le nain venant de relacher la ressource et je le met à la fin
         * laissant sa place au autre nain pour les futures itération
         */
        String temp = FIFO.pollFirst();
        FIFO.addLast(temp);
        libre = true;
        notifyAll();
    }
}

class Nain extends Thread {
    private BlancheNeige bn;
    public Nain(String nom, BlancheNeige bn) {
        this.setName(nom);
        this.bn = bn;
    }
    public void run() {

        while (!isInterrupted()) {
            try {
                bn.requérir();
            } catch (InterruptedException e) {
                fin_thread();
                break;
            }

            try {
                bn.accéder();
            } catch (InterruptedException e) {
                fin_thread();
                break;
            }

            System.out.println(new Date(System.currentTimeMillis())+" "+getName() + " a un accès (exclusif) à Blanche-Neige.");
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                System.out.println(new Date(System.currentTimeMillis())+" "+getName() + " s'apprête à quitter Blanche-Neige.");

                bn.relâcher();
                fin_thread();
                break;
            }
                System.out.println(new Date(System.currentTimeMillis())+" "+getName() + " s'apprête à quitter Blanche-Neige.");
                bn.relâcher();
        }
        System.out.println(new Date(System.currentTimeMillis())+" "+getName() + " a terminé!");
        interrupt();


    }

    /**
     * Fonction appeller à chaque catch Interrupted execution,dans le cas si on souhaite rajouter d'autre comportement
     */
    void fin_thread(){
        this.interrupt();
    }

    @Override
    public void interrupt(){
        super.interrupt();
    }
}

/*
$ make
$ java SeptNains
[09h 34mn 01,834s] Début du programme.
	Simplet veut la ressource.
	Simplet accède à la ressource.
	Timide veut la ressource.
	Prof veut la ressource.
	Grincheux veut la ressource.
	Joyeux veut la ressource.
	Atchoum veut la ressource.
	Dormeur veut la ressource.
Simplet a un accès (exclusif) à Blanche-Neige.
Simplet s'apprête à quitter à Blanche-Neige.
	Simplet relâche la ressource.
	Simplet veut la ressource.
	Simplet accède à la ressource.
Simplet a un accès (exclusif) à Blanche-Neige.
	Timide accède à la ressource.
Timide a un accès (exclusif) à Blanche-Neige.
	Dormeur accède à la ressource.
Dormeur a un accès (exclusif) à Blanche-Neige.
	Atchoum accède à la ressource.
Atchoum a un accès (exclusif) à Blanche-Neige.
	Joyeux accède à la ressource.
Joyeux a un accès (exclusif) à Blanche-Neige.
	Grincheux accède à la ressource.
Grincheux a un accès (exclusif) à Blanche-Neige.
	Prof accède à la ressource.
Prof a un accès (exclusif) à Blanche-Neige.
^C
*/
