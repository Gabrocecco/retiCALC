typedef string String <255>;

const NUMGIUDICI = 4;

struct voto {
    char operazione;
    String candidato;
};

struct classifica {
    String giudice[NUMGIUDICI];
    int punti[NUMGIUDICI];
};

program VOTAFATTOREX {
	version VOTOVERS {
		bool AGGINGI_VOTO(voto) = 1;
		classifica CLASSIFICA_VOTO() = 2;
	} = 1;
} = 0x20000000;
