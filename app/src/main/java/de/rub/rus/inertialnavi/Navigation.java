package de.rub.rus.inertialnavi;


/**
 * Created by pbe on 08.12.2016.
 */
public abstract class Navigation {

    /**
     * Update of the direction cosine matrix (Richtungskosinusmatrix)
     * see Titterton+Weston Strapdown Inertial Navigation Technology 2nd edition (p. 40)
     * @param C_k DCM at time instatnt k
     * @param w_b_ib small rotation which updates the DCM
     * @param T time between two updates
     * @return returns updated DCM at time k+1 (C_k1)
     */
    public static double[] updateDCM (double[] C_k, double[] w_b_ib, double T){
        double[] C_k1 = new double[9];

        //TODO: Erstelle Methode zum Updaten der Richtungskosinusmatrix

        C_k1[0] = 0;
        C_k1[1] = 0;
        C_k1[2] = 0;

        C_k1[3] = 0;
        C_k1[4] = 0;
        C_k1[5] = 0;

        C_k1[6] = 0;
        C_k1[7] = 0;
        C_k1[8] = 0;

        return C_k1;
    }

    /**
     * Rotate vector with DCM
     * @param dcm DCM (Richtungskosinusmatrix)
     * @param inVector input Vector
     * @return output Vector
     */
    public static double[] rotateVectorDCM(double[] dcm, double[] inVector){
        double[] outVector = new double[3];


        //TODO: Matrix-Vektor Multiplikation einfuegen

        return outVector;
    }


    /**
     * Initialisierung der Richtungskosinusmatrix
     * @return initialisierte Richtungskosinusmatrix
     */
    public static double[] initDCM(){
        double[] dcm = new double[9];

        //TODO: Schreiben einer Initialisierungsroutine

        return dcm;
    }


}
