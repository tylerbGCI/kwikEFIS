package player.airspace;


class AirspaceClassStringCouple
{
    String string; //char[] string;
    AirspaceClass type;


    AirspaceClassStringCouple(String s, AirspaceClass c)
    {
        string = s;
        type = c;
    }

    public static final AirspaceClassStringCouple[] airspace_class_strings =
            {
                    new AirspaceClassStringCouple("R", AirspaceClass.RESTRICT),
                    new AirspaceClassStringCouple("Q", AirspaceClass.DANGER),
                    new AirspaceClassStringCouple("P", AirspaceClass.PROHIBITED),
                    new AirspaceClassStringCouple("CTR", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("A", AirspaceClass.CLASSA),
                    new AirspaceClassStringCouple("B", AirspaceClass.CLASSB),
                    new AirspaceClassStringCouple("C", AirspaceClass.CLASSC),
                    new AirspaceClassStringCouple("D", AirspaceClass.CLASSD),
                    new AirspaceClassStringCouple("GP", AirspaceClass.NOGLIDER),
                    new AirspaceClassStringCouple("W", AirspaceClass.WAVE),
                    new AirspaceClassStringCouple("E", AirspaceClass.CLASSE),
                    new AirspaceClassStringCouple("F", AirspaceClass.CLASSF),
                    new AirspaceClassStringCouple("TMZ", AirspaceClass.TMZ),
                    new AirspaceClassStringCouple("G", AirspaceClass.CLASSG),
                    new AirspaceClassStringCouple("RMZ", AirspaceClass.RMZ),
                    new AirspaceClassStringCouple("MATZ", AirspaceClass.MATZ),
                    new AirspaceClassStringCouple("GSEC", AirspaceClass.WAVE)
            };


    public static final AirspaceClassStringCouple[] airspace_tnp_type_strings =
            {
                    new AirspaceClassStringCouple("C", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("CTA", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("CTR", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("CTA/CTR", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("CTR/CTA", AirspaceClass.CTR),
                    new AirspaceClassStringCouple("R", AirspaceClass.RESTRICT),
                    new AirspaceClassStringCouple("RESTRICTED", AirspaceClass.RESTRICT),
                    new AirspaceClassStringCouple("P", AirspaceClass.PROHIBITED),
                    new AirspaceClassStringCouple("PROHIBITED", AirspaceClass.PROHIBITED),
                    new AirspaceClassStringCouple("D", AirspaceClass.DANGER),
                    new AirspaceClassStringCouple("DANGER", AirspaceClass.DANGER),
                    new AirspaceClassStringCouple("G", AirspaceClass.WAVE),
                    new AirspaceClassStringCouple("GSEC", AirspaceClass.WAVE),
                    new AirspaceClassStringCouple("T", AirspaceClass.TMZ),
                    new AirspaceClassStringCouple("TMZ", AirspaceClass.TMZ),
                    new AirspaceClassStringCouple("CYR", AirspaceClass.RESTRICT),
                    new AirspaceClassStringCouple("CYD", AirspaceClass.DANGER),
                    new AirspaceClassStringCouple("CYA", AirspaceClass.CLASSF),
                    new AirspaceClassStringCouple("MATZ", AirspaceClass.MATZ),
                    new AirspaceClassStringCouple("RMZ", AirspaceClass.RMZ)
            };

}
