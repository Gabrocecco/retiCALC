/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#include "/home/studente/Desktop/Es9_fork/votafattorex.h"
#include <stdio.h>
#include <stdlib.h>
#include <rpc/pmap_clnt.h>
#include <string.h>
#include <memory.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include "struct.h"

#ifndef SIG_PF
#define SIG_PF void(*)(int)
#endif

extern l voti[N] = {};

static void
votafattorex_1(struct svc_req *rqstp, register SVCXPRT *transp)
{
	union {
		voto aggingi_voto_1_arg;
	} argument;
	char *result;
	xdrproc_t _xdr_argument, _xdr_result;
	char *(*local)(char *, struct svc_req *);

	switch (rqstp->rq_proc) {
	case NULLPROC:
		(void) svc_sendreply (transp, (xdrproc_t) xdr_void, (char *)NULL);
		return;

	case AGGINGI_VOTO:
		_xdr_argument = (xdrproc_t) xdr_voto;
		_xdr_result = (xdrproc_t) xdr_bool;
		local = (char *(*)(char *, struct svc_req *)) aggingi_voto_1_svc;
		break;

	case CLASSIFICA_VOTO:
		_xdr_argument = (xdrproc_t) xdr_void;
		_xdr_result = (xdrproc_t) xdr_classifica;
		local = (char *(*)(char *, struct svc_req *)) classifica_voto_1_svc;
		break;

	default:
		svcerr_noproc (transp);
		return;
	}
	memset ((char *)&argument, 0, sizeof (argument));
	if (!svc_getargs (transp, (xdrproc_t) _xdr_argument, (caddr_t) &argument)) {
		svcerr_decode (transp);
		return;
	}
	result = (*local)((char *)&argument, rqstp);
	if (result != NULL && !svc_sendreply(transp, (xdrproc_t) _xdr_result, result)) {
		svcerr_systemerr (transp);
	}
	if (!svc_freeargs (transp, (xdrproc_t) _xdr_argument, (caddr_t) &argument)) {
		fprintf (stderr, "%s", "unable to free arguments");
		exit (1);
	}
	return;
}

int
main (int argc, char **argv)
{
	register SVCXPRT *transp;

	pmap_unset (VOTAFATTOREX, VOTOVERS);

	transp = svcudp_create(RPC_ANYSOCK);
	if (transp == NULL) {
		fprintf (stderr, "%s", "cannot create udp service.");
		exit(1);
	}
	if (!svc_register(transp, VOTAFATTOREX, VOTOVERS, votafattorex_1, IPPROTO_UDP)) {
		fprintf (stderr, "%s", "unable to register (VOTAFATTOREX, VOTOVERS, udp).");
		exit(1);
	}

	transp = svctcp_create(RPC_ANYSOCK, 0, 0);
	if (transp == NULL) {
		fprintf (stderr, "%s", "cannot create tcp service.");
		exit(1);
	}
	if (!svc_register(transp, VOTAFATTOREX, VOTOVERS, votafattorex_1, IPPROTO_TCP)) {
		fprintf (stderr, "%s", "unable to register (VOTAFATTOREX, VOTOVERS, tcp).");
		exit(1);
	}

	for (int i = 0; i < N; i++) {
		strcpy(voti[i].candidato, "L");
		strcpy(voti[i].giudice, "L");
		strcpy(voti[i].nomeFile, "L");
		voti[i].categoria = 'L';
		voti[i].fase = 'L';
		voti[i].voto = -1;
	}

	//Sample Data

	strcpy(voti[0].candidato, "Johnny");
	strcpy(voti[0].giudice, "Volgin");
	strcpy(voti[0].nomeFile, "johnny.txt");
	voti[0].categoria = 'U';
	voti[0].fase = 'B';
	voti[0].voto = 1;

	strcpy(voti[1].candidato, "John");
	strcpy(voti[1].giudice, "Zero");
	strcpy(voti[1].nomeFile, "naked_snake.txt");
	voti[1].categoria = 'U';
	voti[1].fase = 'S';
	voti[1].voto =64;

	strcpy(voti[2].candidato, "Shalashaska");
	strcpy(voti[2].giudice, "Lalilulelo");
	strcpy(voti[2].nomeFile, "ocelot.txt");
	voti[2].categoria = 'U';
	voti[2].fase = 'S';
	voti[2].voto = 54;

	strcpy(voti[3].candidato, "The End");
	strcpy(voti[3].giudice, "The Boss");
	strcpy(voti[3].nomeFile, "mosin-nagant.txt");
	voti[3].categoria = 'O';
	voti[3].fase = 'S';
	voti[3].voto = 100;

	strcpy(voti[4].candidato, "The Sorrow");
	strcpy(voti[4].giudice, "The Boss");
	strcpy(voti[4].nomeFile, "medium.txt");
	voti[4].categoria = 'O';
	voti[4].fase = 'S';
	voti[4].voto = 34;


	svc_run ();
	fprintf (stderr, "%s", "svc_run returned");
	exit (1);
	/* NOTREACHED */
}
