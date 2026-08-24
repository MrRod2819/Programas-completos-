import { Router } from 'express';
import { receiveWebhook, verifyWebhook } from '../controllers/webhookController';
import { verifyMetaSignature } from '../middleware/verifyMetaSignature';

export const webhookRouter = Router();

webhookRouter.get('/webhook', verifyWebhook);
webhookRouter.post('/webhook', verifyMetaSignature, receiveWebhook);
