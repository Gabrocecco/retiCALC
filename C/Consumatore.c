#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#define MAX_STRING_LENGTH 256

int main(int argc, char* argv[]){

	char *file_in, read_char, *pref;
	int nread, fd;
    int check[256] = {0}; //Inizializzo un array di zero per il controllo dei caratteri da tagliare
	
	//controllo numero argomenti
	if (argc != 2 && argc != 3){ 
		perror(" numero di argomenti sbagliato"); exit(1);
	} 
	
	pref = argv[1];
    
    //Per evitare di iterare ogni volta sull'array pref per controllare se i caratteri sono presenti,
    //Inserisco 1 (vero) nelle posizioni indicate dalla codifica ASCII dei caratteri inseriti dall'utente
    
    for (int i = 0; i < strlen(pref); i++) {
        check[pref[i]] = 1;
    }
	
	//Nel caso in cui l'utente abbia inserito due argomenti, ridireziono l'input sul file indicato (se esiste)
	//Così da non dover differenziare il codice
	
	if (argc == 3) {
        file_in = argv[2];
        fd = open(file_in, O_RDONLY);
        if (fd<0){
            perror("P0: Impossibile aprire il file.");
            exit(2);
        }
        close(0);
        dup(fd);
        close(fd);
    }
	
	while (nread = read(0, &read_char, 1)) {
		if (nread >= 0) {
            if (!check[read_char]) {  //Controllo dei caratteri da filtrare
                putchar(read_char);
            }
        }
		else{
			printf("(PID %d) impossibile leggere dal file %s", getpid(), file_in);
			perror("Errore!");
			close(fd);
			exit(3);
		}
	}
}
